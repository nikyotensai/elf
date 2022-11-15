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

package com.github.nikyotensai.elf.remoting.netty;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.agent.common.pid.PidUtils;
import com.github.nikyotensai.elf.common.ElfConstants;
import com.github.nikyotensai.elf.remoting.command.CommandSerializer;
import com.github.nikyotensai.elf.remoting.protocol.CodeTypeMappingStore;
import com.github.nikyotensai.elf.remoting.protocol.CodeTypeMappingStores;
import com.github.nikyotensai.elf.remoting.protocol.Datagram;
import com.github.nikyotensai.elf.remoting.protocol.ErrorCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingHeader;
import com.github.nikyotensai.elf.remoting.protocol.ResponseCode;
import com.google.common.collect.ImmutableMap;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhenyu.nie created on 2018 2018/10/22 17:30
 */
@ChannelHandler.Sharable
@Slf4j
public class RequestHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Map<Integer, Processor> processorMap;
    private final CodeTypeMappingStore codeTypeMappingStore = CodeTypeMappingStores.getInstance();

    public RequestHandler(List<Processor> processors) {
        ImmutableMap.Builder<Integer, Processor> builder = new ImmutableMap.Builder<>();
        for (Processor<?> processor : processors) {
            for (Integer type : processor.types()) {
                builder.put(type, processor);
            }
        }
        processorMap = builder.build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("request process error", cause);
        String requestId = ctx.channel().attr(AgentConstants.attributeKey).get();
        if (requestId != null) {
            RemotingHeader requestHeader = new RemotingHeader();
            requestHeader.setId(requestId);
            String errorMessage = "request process error, " + cause.getClass().getName() + ": " + cause.getMessage();
            ResponseWriter.getInstance().writeEOF(ctx, errorMessage, requestHeader);
        }
        ctx.channel().close();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        final Datagram datagram = (Datagram) msg;
        RemotingHeader header = datagram.getHeader();
        int code = header.getCode();
        String id = header.getId();

        if (code != ResponseCode.RESP_TYPE_HEARTBEAT.getCode()) {
            logger.info("agent receive request: id={}, sourceIp={}, code={}", id, ctx.channel().remoteAddress(), code);
        }
        final ResponseHandler handler = NettyExecuteHandler.of(header, ctx);
        ctx.channel().attr(AgentConstants.attributeKey).set(id);

        Processor processor = processorMap.get(code);
        if (processor == null) {
            handler.handleError(new IllegalArgumentException("unknown code [" + code + "]"));
            return;
        }

        String command = CommandSerializer.readCommand(datagram.getBody());
        int index = command.indexOf(ElfConstants.FILL_PID);
        if (index >= 0) {
            int pid = PidUtils.getPid();
            if (pid < 0) {
                handler.handleError(ErrorCode.PID_ERROR.getCode());
                handler.handleEOF();
                log.error("pid[{}] is error,please check", pid);
                return;
            }
            command = command.replace(ElfConstants.FILL_PID, String.valueOf(pid));
        }

        Class<?> commandType = codeTypeMappingStore.getMappingType(code);

        processor.process(header, commandType.cast(CommandSerializer.deserializeCommand(command, commandType)), handler);
    }
}
