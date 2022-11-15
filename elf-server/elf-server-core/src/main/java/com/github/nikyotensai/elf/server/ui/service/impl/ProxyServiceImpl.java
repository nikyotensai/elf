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

package com.github.nikyotensai.elf.server.ui.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.application.api.SlotService;
import com.github.nikyotensai.elf.server.config.ElfServerProperties;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnectionStore;
import com.github.nikyotensai.elf.server.ui.service.ProxyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProxyServiceImpl implements ProxyService {


    @Autowired
    private AgentConnectionStore agentConnectionStore;
    @Autowired
    private SlotService slotServices;
    @Autowired
    private ElfServerProperties elfProxyProperties;


    @Override
    public String getWebSocketUrl(String agentIp, String host) {
        // 访问一下agent接口，判断agent是否存在
        Optional<AgentConnection> connection = agentConnectionStore.getConnection(agentIp);
        if (!connection.isPresent() || !connection.get().isActive()) {
            slotServices.beforeGetSocket(host);
        }
        return elfProxyProperties.getServer().getWebSocketUrl();
    }


}
