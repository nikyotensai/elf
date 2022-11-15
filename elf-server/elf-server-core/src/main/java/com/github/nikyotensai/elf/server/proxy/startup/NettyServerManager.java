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

package com.github.nikyotensai.elf.server.proxy.startup;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.nikyotensai.elf.application.api.AppServerService;
import com.github.nikyotensai.elf.server.config.ElfServerProperties;
import com.github.nikyotensai.elf.server.config.ElfServerProperties.ServerConf;
import com.github.nikyotensai.elf.server.proxy.communicate.Connection;
import com.github.nikyotensai.elf.server.proxy.communicate.SessionManager;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnectionStore;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.NettyServerForAgent;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.handler.AgentMessageHandler;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.handler.AgentMessageProcessor;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.NettyServerForUi;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.UiConnectionStore;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.command.CommunicateCommandStore;
import com.github.nikyotensai.elf.server.proxy.generator.IdGenerator;

import lombok.extern.slf4j.Slf4j;

/**
 * @author leix.xie
 * @since 2019-07-18 11:32
 */
@Component
@Slf4j
public class NettyServerManager {


    @Autowired
    private CommunicateCommandStore commandStore;

    @Autowired
    private UiConnectionStore uiConnectionStore;

    @Autowired
    private AgentConnectionStore agentConnectionStore;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private AppServerService appServerService;

    @Autowired
    private IdGenerator sessionIdGenerator;

    @Autowired
    private List<AgentMessageProcessor> agentMessageProcessors;

    private NettyServerForAgent nettyServerForAgent;

    private NettyServerForUi nettyServerForUi;

    @Autowired
    ElfServerProperties proxyEnvMapConfig;

    @PostConstruct
    public void start() {
        ServerConf conf = proxyEnvMapConfig.getServer();
        nettyServerForAgent = startAgentServer(conf);
        nettyServerForUi = startUiServer(conf);
    }

    @PreDestroy
    public void stop() {
        offline();
        nettyServerForUi.stop();
        nettyServerForAgent.stop();
    }

    private NettyServerForUi startUiServer(ServerConf conf) {
        NettyServerForUi serverForUi = new NettyServerForUi(
                conf,
                sessionIdGenerator,
                commandStore,
                uiConnectionStore,
                agentConnectionStore,
                sessionManager,
                appServerService);
        serverForUi.start();
        return serverForUi;
    }

    private NettyServerForAgent startAgentServer(ServerConf conf) {
        AgentMessageHandler handler = new AgentMessageHandler(agentMessageProcessors);
        NettyServerForAgent serverForAgent = new NettyServerForAgent(conf, handler);
        serverForAgent.start();
        return serverForAgent;
    }

    private void closeAgentConnections() {
        Map<String, AgentConnection> agentConnection = agentConnectionStore.getAgentConnection();
        Collection<AgentConnection> connections = agentConnection.values();
        for (Connection connection : connections) {
            connection.close();
        }
    }


    public boolean offline() {
        closeAgentConnections();
        return true;
    }

}
