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

package com.github.nikyotensai.elf.server.proxy.communicate.ui.handler;

import java.util.Iterator;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.github.nikyotensai.elf.remoting.protocol.AgentServerInfo;
import com.github.nikyotensai.elf.remoting.protocol.RequestData;
import com.github.nikyotensai.elf.server.pojo.AppServer;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.UiResponses;
import com.github.nikyotensai.elf.server.proxy.util.ServerFinder;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author leix.xie
 * @since 2019/5/23 11:20
 */
@ChannelHandler.Sharable
public class HostsValidatorHandler extends ChannelInboundHandlerAdapter {

    private final ServerFinder serverFinder;

    public HostsValidatorHandler(ServerFinder serverFinder) {
        this.serverFinder = serverFinder;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestData requestData = (RequestData) msg;
        List<String> hosts = requestData.getHosts();
        if (CollectionUtils.isEmpty(hosts)) {
            ctx.writeAndFlush(UiResponses.createNoHostResponse(requestData));
            return;
        }
        hostValidator(requestData, ctx);
    }

    // ?????? app ??? host ?????????
    private void hostValidator(final RequestData requestData, ChannelHandlerContext ctx) {
        List<AppServer> servers = serverFinder.findAgents(requestData.getApp());
        List<String> userHosts = requestData.getHosts();
        List<AppServer> ret = servers;
        if (userHosts != null && !userHosts.isEmpty()) {
            ret = Lists.newArrayList();
            for (AppServer server : servers) {
                for (String host : userHosts) {
                    if (server.getHost().equals(host)) {
                        ret.add(server);
                    }
                }
            }
        }

        // ????????? common-core, ????????? logdir ??? server ??????
        Iterator<AppServer> iterator = ret.iterator();
        while (iterator.hasNext()) {
            AppServer next = iterator.next();
            if (Strings.isNullOrEmpty(next.getLogDir())) {
                iterator.remove();
                ctx.writeAndFlush(UiResponses.createNoLogDirResponse(requestData, next.getIp()));
            }
        }

        if (ret.isEmpty()) {
            ctx.writeAndFlush(UiResponses.createHostValidateErrorResponse(requestData));
        } else {
            List<AgentServerInfo> serverInfos = Lists.transform(ret, (server) -> {
                AgentServerInfo agentServerInfo = new AgentServerInfo();
                agentServerInfo.setAgentId(server.getIp());
                agentServerInfo.setIp(server.getIp());
                agentServerInfo.setAppcode(server.getAppCode());
                agentServerInfo.setHost(server.getHost());
                agentServerInfo.setLogdir(server.getLogDir());
                agentServerInfo.setPort(server.getPort());
                return agentServerInfo;
            });
            requestData.setAgentServerInfos(serverInfos);
            ctx.fireChannelRead(requestData);
        }
    }
}
