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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.nikyotensai.elf.server.bean.ApiResult;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnectionStore;
import com.github.nikyotensai.elf.server.util.ResultHelper;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;

/**
 * @author leix.xie
 * @since 2019/6/21 12:00
 */
@Controller
@RequestMapping("/proxy/agent/version")
public class AgentVersionController {

    @Autowired
    private AgentConnectionStore agentConnectionStore;

    @ResponseBody
    @RequestMapping("detail")
    public ApiResult getAllAgentConnection() {
        return ResultHelper.success(agentConnectionStore.getAgentConnection());
    }

    @ResponseBody
    @RequestMapping("search")
    public ApiResult getAgentConnection(@RequestParam("agentId") final String agentId) {
        Map<String, AgentConnection> connection = agentConnectionStore.searchConnection(agentId);
        if (!CollectionUtils.isEmpty(connection)) {
            return ResultHelper.success(connection);
        }
        return ResultHelper.fail(-1, "没有查询到 " + agentId + " 相关版本信息");
    }

    @ResponseBody
    @RequestMapping("abstract")
    public ApiResult count() {
        Map<String, AgentConnection> agentConnection = agentConnectionStore.getAgentConnection();
        Map<Integer, List<AgentConnection>> collect = agentConnection.values().stream().collect(Collectors.groupingBy(AgentConnection::getVersion));
        ImmutableSortedMap<Integer, Integer> result = ImmutableSortedMap.<Integer, Integer>naturalOrder().putAll(Maps.transformValues(collect, List::size)).build();
        return ResultHelper.success(result);
    }
}
