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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.common.ElfConstants;
import com.github.nikyotensai.elf.remoting.protocol.AgentServerInfo;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.Datagram;
import com.github.nikyotensai.elf.remoting.protocol.RemotingBuilder;
import com.github.nikyotensai.elf.remoting.protocol.RequestData;
import com.github.nikyotensai.elf.remoting.protocol.payloadHolderImpl.RequestPayloadHolder;
import com.github.nikyotensai.elf.server.proxy.communicate.Session;
import com.github.nikyotensai.elf.server.proxy.communicate.SessionManager;
import com.github.nikyotensai.elf.server.proxy.communicate.WritableListener;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnectionStore;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.UiConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.UiConnectionStore;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.UiResponses;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.command.CommunicateCommand;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.command.CommunicateCommandStore;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.commandprocessor.CommunicateCommandProcessor;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author zhenyu.nie created on 2019 2019/5/16 16:12
 */
@ChannelHandler.Sharable
public class UiRequestHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(UiRequestHandler.class);

    private final UiConnectionStore uiConnectionStore;

    private final AgentConnectionStore agentConnectionStore;

    private final SessionManager sessionManager;

    private final CommunicateCommandStore commandStore;

    public UiRequestHandler(CommunicateCommandStore commandStore,
                            UiConnectionStore uiConnectionStore,
                            AgentConnectionStore agentConnectionStore,
                            SessionManager sessionManager) {
        this.commandStore = commandStore;
        this.uiConnectionStore = uiConnectionStore;
        this.agentConnectionStore = agentConnectionStore;
        this.sessionManager = sessionManager;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Datagram)) {
            super.write(ctx, msg, promise);
            return;
        }

        Datagram datagram = (Datagram) msg;

        String id = Strings.nullToEmpty(datagram.getHeader().getId());
        Session session = sessionManager.getSession(id);
        if (session == null) {
            super.write(ctx, msg, promise);
            return;
        }

        Optional<CommunicateCommand> communicateCommand = commandStore.getCommunicateCommand(session.getRequestData().getType());
        if (!communicateCommand.isPresent()) {
            logger.warn("unknown command response, {}", datagram);
            session.finish();
            return;
        }

        Datagram response = communicateCommand.get().getProcessor().prepareResponse(datagram);
        super.write(ctx, response, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof RequestData)) {
            super.channelRead(ctx, msg);
            return;
        }

        @SuppressWarnings("unchecked")
        RequestData<String> inputData = (RequestData<String>) msg;

        UiConnection uiConnection = uiConnectionStore.register(ctx.channel());

        if (inputData.getType() == CommandCode.REQ_TYPE_CANCEL.getCode()) {
            cancelRequest(uiConnection);
            return;
        }

        Optional<CommunicateCommand> command = commandStore.getCommunicateCommand(inputData.getType());
        if (!command.isPresent()) {
            ctx.channel().writeAndFlush(UiResponses.createNoCommandResponse(inputData));
            return;
        }

        CommunicateCommand communicateCommand = command.get();
        if (!communicateCommand.isSupportMulti() && inputData.getAgentServerInfos().size() > 1) {
            ctx.channel().writeAndFlush(UiResponses.createNotSupportMultiResponse(inputData));
            return;
        }

        CommunicateCommandProcessor<?> processor = communicateCommand.getProcessor();
        Optional<? extends RequestData<?>> requestDataOptional = preProcessor(processor, inputData, ctx);
        if (!requestDataOptional.isPresent()) {
            return;
        }

        RequestData<?> requestData = requestDataOptional.get();
        List<AgentConnection> agentConnections = Lists.newArrayListWithCapacity(requestData.getAgentServerInfos().size());
        List<String> lessVersionAgents = Lists.newArrayList();
        List<String> noConnectionAgents = Lists.newArrayList();
        for (AgentServerInfo agentServerInfo : requestData.getAgentServerInfos()) {
            Optional<AgentConnection> agentConnection = agentConnectionStore.getConnection(agentServerInfo.getAgentId());
            if (agentConnection.isPresent()) {
                if (agentConnection.get().getVersion() >= communicateCommand.getMinAgentVersion()) {
                    agentConnections.add(agentConnection.get());
                } else {
                    lessVersionAgents.add(agentServerInfo.getAgentId());
                }
            } else {
                noConnectionAgents.add(agentServerInfo.getAgentId());
            }
        }

        noConnectionAgents.stream()
                .map(noConnectionAgent -> UiResponses.createNoConnectionResponse(noConnectionAgent, requestData))
                .forEach(uiConnection::write);
        lessVersionAgents.stream().
                map(lessVersionAgent -> UiResponses.createLessVersionResponse(lessVersionAgent, requestData))
                .forEach(uiConnection::write);

        if (agentConnections.isEmpty()) {
            uiConnection.write(UiResponses.createFinishResponse(requestData));
            return;
        }

        List<Session> sessions = agentConnections.stream()
                .map((agentConnection -> sendMessage(command.get(), requestData, processor, agentConnection, uiConnection)))
                .collect(Collectors.toList());

        ListenableFuture<List<Session.State>> sessionsFuture = Futures.successfulAsList(sessions.stream().map(Session::getEndState).collect(Collectors.toList()));
        sessionsFuture.addListener(() -> uiConnection.write(UiResponses.createFinishResponse(requestData)), MoreExecutors.directExecutor());
    }

    private Optional<? extends RequestData<?>> preProcessor(CommunicateCommandProcessor<?> processor, RequestData<String> inputData, ChannelHandlerContext ctx) {
        try {
            Optional<? extends RequestData<?>> requestData = processor.preprocessor(inputData, ctx);
            if (!requestData.isPresent()) {
                ctx.channel().writeAndFlush(UiResponses.createProcessRequestErrorResponse(inputData));
            }
            return requestData;
        } catch (Exception e) {
            ctx.channel().writeAndFlush(UiResponses.createProcessRequestErrorResponse(inputData, "\033[31m[ERROR]:\033[0m Command preprocess failed: " + e.getMessage()));
            logger.error("pre processor command fail", e);
            return Optional.empty();
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        boolean writable = channel.isWritable();
        uiConnectionStore.getConnection(channel).ifPresent(
                uiConnection -> uiConnection.setWritable(writable));
        super.channelWritabilityChanged(ctx);
    }

    private Session sendMessage(CommunicateCommand command, RequestData requestData, CommunicateCommandProcessor<?> processor, AgentConnection agentConnection, UiConnection uiConnection) {
        Session session = sessionManager.create(command, requestData, agentConnection, uiConnection);
        @SuppressWarnings("unchecked")
        Datagram datagram = processor.prepareRequest(session.getId(), requestData, agentConnection.getAgentId());
        if (!agentConnection.isWritable()) {
            logger.info("agent connection is not writable, {}, {}", agentConnection, datagram);
            uiConnection.write(UiResponses.createAgentCannotConnect(requestData));
            session.broken();
            return session;
        }

        session.writeToAgent(datagram);
        if (session.isSupportPause()) {
            UiWritableListener listener = new UiWritableListener(session);
            uiConnection.addWritableListener(listener);
            session.getEndState().addListener(
                    () -> uiConnection.removeWritableListener(listener),
                    MoreExecutors.directExecutor());
        }
        return session;
    }

    private static class UiWritableListener implements WritableListener {

        private final Session session;

        private boolean writable = true;

        private UiWritableListener(Session session) {
            this.session = session;
        }

        @Override
        public void onChange(boolean writable) {
            if (this.writable != writable) {
                this.writable = writable;
                if (session.getAgentConnection().getVersion() >= ElfConstants.MIN_AGENT_VERSION_SUPPORT_JOB_PAUSE) {
                    CommandCode code = writable ? CommandCode.REQ_TYPE_JOB_RESUME : CommandCode.REQ_TYPE_JOB_PAUSE;
                    Datagram datagram = RemotingBuilder.buildRequestDatagram(code.getCode(), session.getId(), new RequestPayloadHolder(session.getId()));
                    session.writeToAgent(datagram);
                }
            }
        }
    }

    private void cancelRequest(UiConnection uiConnection) {
        Set<Session> sessions = sessionManager.getSessionByUiConnection(uiConnection);
        for (Session session : sessions) {
            String id = session.getId();
            Datagram datagram = RemotingBuilder.buildRequestDatagram(CommandCode.REQ_TYPE_CANCEL.getCode(), id, new RequestPayloadHolder(id));
            session.writeToAgent(datagram);
            session.finish();
        }
    }

}
