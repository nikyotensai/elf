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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.nikyotensai.elf.server.bean.ApiResult;
import com.github.nikyotensai.elf.server.config.ElfServerProperties;
import com.github.nikyotensai.elf.server.util.ResultHelper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author leix.xie
 * @since 2019/5/23 16:22
 */
@Controller
@Slf4j
public class ProxyConfigForAgentGetController {

    @Autowired
    private ElfServerProperties elfProxyProperties;

    private ProxyConfig proxyConfig;

    @PostConstruct
    public void init() {
        proxyConfig = new ProxyConfig(
                elfProxyProperties.getServer().getHost(),
                elfProxyProperties.getServer().getPort4Agent(),
                elfProxyProperties.getServer().getHeartbeatSec());
    }


    /**
     * agent 启动时请求这个地址获取 proxy 的 websocket 地址
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/proxy/config/foragent")
    public ApiResult<ProxyConfig> getProxyConfig() {
        return ResultHelper.success(proxyConfig);
    }

    @Data
    private static class ProxyConfig {

        private final String ip;

        private final int port;

        private final int heartbeatSec;

        private ProxyConfig(String ip, int port, int heartbeatSec) {
            this.ip = ip;
            this.port = port;
            this.heartbeatSec = heartbeatSec;
        }


    }
}
