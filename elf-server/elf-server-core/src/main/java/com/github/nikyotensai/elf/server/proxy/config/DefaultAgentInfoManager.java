/*
 * Copyright (C) 2019 Qunar, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.nikyotensai.elf.server.proxy.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.application.api.AppServerService;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingBuilder;
import com.github.nikyotensai.elf.server.pojo.AppServer;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnectionStore;
import com.github.nikyotensai.elf.server.proxy.generator.IdGenerator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author zhenyu.nie created on 2019 2019/5/15 14:21
 */
@Service
public class DefaultAgentInfoManager implements AgentInfoManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAgentInfoManager.class);

    private Map<String, String> agentConfig = new HashMap() {{
        put("agent.refresh.interval.min", "10");
        put("agent.push.interval.min=", "1");
        put("app.config.exclusion.file.suffix", "class,vm,css,js,vue,ts,jsp,sql,jar");
        put("app.config.exclusion.file.equal=", "web.xml");
        put("debug.json.limit.kb", "10240");
        put("download.kb.per.second", "10240");
    }};

    @Autowired
    private IdGenerator generator;

    @Autowired
    private AgentConnectionStore agentConnectionStore;

    @Autowired
    private AppServerService appServerService;


    @Override
    public ListenableFuture<Map<String, String>> getAgentInfo(String ip) {
        SettableFuture<Map<String, String>> resultFuture = SettableFuture.create();
        AppServer appServer = this.appServerService.getAppServerByIp(ip);
        Map<String, String> agentInfo = new HashMap<>();
        if (appServer != null) {
            agentInfo.put("port", String.valueOf(appServer.getPort()));
            agentInfo.put("cpuJStackOn", String.valueOf(appServer.isAutoJStackEnable()));
        }
        //这里可以覆盖所有版本配置
        agentInfo.putAll(agentConfig);

        resultFuture.set(agentInfo);
        return resultFuture;
    }

    @Override
    public void updateAgentInfo(List<String> agentIds) {
        agentIds.forEach(agentId -> {
            Optional<AgentConnection> optionalAgentConnection = agentConnectionStore.getConnection(agentId);
            if (optionalAgentConnection.isPresent()) {
                logger.info("notify agent {} update meta info ", agentId);
                AgentConnection agentConnection = optionalAgentConnection.get();
                agentConnection.write(RemotingBuilder.buildRequestDatagram(CommandCode.REQ_TYPE_REFRESH_TIP.getCode(), generator.generateId(), null));
            }
        });
    }

    private int getVersion(String ip) {
        Optional<AgentConnection> connection = agentConnectionStore.getConnection(ip);
        if (connection.isPresent()) {
            return connection.get().getVersion();
        }
        return -1;
    }
}
