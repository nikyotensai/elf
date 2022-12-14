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

package com.github.nikyotensai.elf.server.proxy.web.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.nikyotensai.elf.server.bean.ApiResult;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnectionStore;
import com.github.nikyotensai.elf.server.util.ResultHelper;
import com.google.common.base.Strings;

/**
 * @author leix.xie
 * @since 2019/5/23 12:03
 */
@Controller
public class AgentGetController {

    @Autowired
    private AgentConnectionStore agentConnectionStore;

    @ResponseBody
    @RequestMapping("/proxy/agent/get")
    public ApiResult getAgentInfo(@RequestParam final String ip) {
        if (Strings.isNullOrEmpty(ip)) {
            return ResultHelper.fail(-1, "argument error", null);
        }
        Optional<AgentConnection> connection = agentConnectionStore.getConnection(ip);
        if (connection.isPresent() && connection.get().isActive()) {
            AgentInfo agentInfo = new AgentInfo(ip);
            return ResultHelper.success(agentInfo);
        }

        return ResultHelper.fail(-1, "no agent", null);
    }

    private static class AgentInfo {

        private final String ip;

        public AgentInfo(String ip) {
            this.ip = ip;
        }

        public String getIp() {
            return ip;
        }
    }
}
