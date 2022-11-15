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

import static com.github.nikyotensai.elf.remoting.protocol.ResponseCode.RESP_TYPE_CONTENT;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.remoting.protocol.Datagram;
import com.github.nikyotensai.elf.remoting.protocol.ResponseCode;
import com.github.nikyotensai.elf.server.proxy.communicate.Session;
import com.github.nikyotensai.elf.server.proxy.communicate.SessionManager;
import com.google.common.collect.ImmutableSet;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author zhenyu.nie created on 2019 2019/5/14 18:12
 */
@Service
public class AgentResponseProcessor implements AgentMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AgentResponseProcessor.class);

    @Autowired
    private SessionManager sessionManager;

    @Override
    public Set<Integer> codes() {
        return ImmutableSet.of(
                RESP_TYPE_CONTENT.getCode(),
                ResponseCode.RESP_TYPE_EXCEPTION.getCode(),
                ResponseCode.RESP_TYPE_SINGLE_END.getCode()
        );
    }

    @Override
    public void process(ChannelHandlerContext ctx, Datagram message) {
        String id = message.getHeader().getId();
        Session session = sessionManager.getSession(id);
        if (session != null) {
            session.writeToUi(message);
        } else {
            logger.warn("id [{}] can not get session, write response fail, {}", id, ctx.channel());
        }
    }

}
