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

import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.remoting.protocol.Datagram;
import com.github.nikyotensai.elf.remoting.protocol.RemotingBuilder;
import com.github.nikyotensai.elf.remoting.protocol.ResponseCode;
import com.github.nikyotensai.elf.remoting.protocol.payloadHolderImpl.RequestPayloadHolder;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnectionStore;
import com.github.nikyotensai.elf.server.proxy.generator.IdGenerator;
import com.github.nikyotensai.elf.server.proxy.util.ChannelUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author zhenyu.nie created on 2019 2019/5/14 17:39
 */
@Service
public class ProxyHeartbeatProcessor implements AgentMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ProxyHeartbeatProcessor.class);

    private static final String HEARTBEAT_SIGN = ".h";

    @Autowired
    private AgentConnectionStore connectionStore;

    @Autowired
    private IdGenerator idGenerator;

    private Datagram heartbeatResponse;

    @PostConstruct
    public void init() {
        heartbeatResponse = initHeartbeatResponse();
    }

    @Override
    public Set<Integer> codes() {
        return ImmutableSet.of(ResponseCode.RESP_TYPE_HEARTBEAT.getCode());
    }

    @Override
    public void process(ChannelHandlerContext ctx, Datagram message) {
        logger.debug("receive heartbeat, {}", message);
        String ip = getIp(message, ctx.channel());
        message.release();
        connectionStore.register(ip, message.getHeader().getAgentVersion(), ctx.channel());
        ctx.channel().writeAndFlush(heartbeatResponse);
    }


    private String getIp(Datagram datagram, Channel channel) {
        final ByteBuf byteBuf = datagram.getBody();
        final String ip = byteBuf.toString(Charsets.UTF_8);
        if (Strings.isNullOrEmpty(ip)) {
            return ChannelUtils.getIp(channel);
        }
        return ip;
    }

    private Datagram initHeartbeatResponse() {
        return RemotingBuilder.buildRequestDatagram(ResponseCode.RESP_TYPE_HEARTBEAT.getCode(), idGenerator.generateId() + HEARTBEAT_SIGN, new RequestPayloadHolder(""));
    }
}
