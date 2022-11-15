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

package com.github.nikyotensai.elf.server.ui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.nikyotensai.elf.server.bean.ApiResult;
import com.github.nikyotensai.elf.server.ui.service.ProxyService;
import com.github.nikyotensai.elf.server.util.ResultHelper;
import com.google.common.base.Strings;

@Controller
public class ConfigController {


    @Autowired
    private ProxyService proxyService;

    @RequestMapping("getProxyWebSocketUrl")
    @ResponseBody
    public ApiResult<String> getProxyWebSocketUrl(@RequestParam String agentIp, @RequestParam String host) {
        if (Strings.isNullOrEmpty(agentIp)) {
            return ResultHelper.fail(-2, "no agent ip");
        }
        //status 为100是new proxy, 0是old proxy
        return ResultHelper.success(100, "new proxy", proxyService.getWebSocketUrl(agentIp, host));
    }
}
