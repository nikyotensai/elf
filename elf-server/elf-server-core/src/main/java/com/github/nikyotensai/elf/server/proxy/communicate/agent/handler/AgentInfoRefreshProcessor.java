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

package com.github.nikyotensai.elf.server.proxy.communicate.agent.handler;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.common.JacksonSerializer;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.Datagram;
import com.github.nikyotensai.elf.remoting.protocol.RemotingBuilder;
import com.github.nikyotensai.elf.remoting.protocol.payloadHolderImpl.RequestPayloadHolder;
import com.github.nikyotensai.elf.server.metrics.Metrics;
import com.github.nikyotensai.elf.server.proxy.config.AgentInfoManager;
import com.github.nikyotensai.elf.server.proxy.generator.IdGenerator;
import com.github.nikyotensai.elf.server.proxy.util.ChannelUtils;
import com.github.nikyotensai.elf.server.proxy.util.FutureSuccessCallBack;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author zhenyu.nie created on 2019 2019/5/15 13:45
 */
@Service
public class AgentInfoRefreshProcessor implements AgentMessageProcessor {

    @Autowired
    private AgentInfoManager agentInfoManager;

    @Autowired
    private IdGenerator generator;

    @Override
    public Set<Integer> codes() {
        return ImmutableSet.of(CommandCode.REQ_TYPE_REFRESH_AGENT_INFO.getCode());
    }

    @Override
    public void process(final ChannelHandlerContext ctx, Datagram message) {
        Metrics.counter("agent_info_refresh").inc();
        String ip = getIp(message, ctx.channel());
        ListenableFuture<Map<String, String>> agentInfoFuture = agentInfoManager.getAgentInfo(ip);
        Futures.addCallback(agentInfoFuture, (FutureSuccessCallBack<Map<String, String>>) agentInfo ->
                Optional.ofNullable(agentInfo)
                        .map(AgentInfoRefreshProcessor.this::createAgentInfoResponse)
                        .ifPresent((ctx::writeAndFlush)), MoreExecutors.directExecutor());
    }

    private Datagram createAgentInfoResponse(Map<String, String> agentInfo) {
        String data = JacksonSerializer.serialize(agentInfo);
        return RemotingBuilder.buildRequestDatagram(CommandCode.REQ_TYPE_REFRESH_AGENT_INFO.getCode(), generator.generateId(), new RequestPayloadHolder(data));
    }

    private String getIp(Datagram datagram, Channel channel) {
        final ByteBuf byteBuf = datagram.getBody();
        final String ip = byteBuf.toString(Charsets.UTF_8);
        if (Strings.isNullOrEmpty(ip)) {
            return ChannelUtils.getIp(channel);
        }
        return ip;
    }

}
