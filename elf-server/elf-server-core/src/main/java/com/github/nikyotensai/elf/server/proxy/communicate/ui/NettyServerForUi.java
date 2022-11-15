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

package com.github.nikyotensai.elf.server.proxy.communicate.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.application.api.AppServerService;
import com.github.nikyotensai.elf.common.Throwables;
import com.github.nikyotensai.elf.server.common.encryption.DefaultRequestEncryption;
import com.github.nikyotensai.elf.server.common.encryption.RSAEncryption;
import com.github.nikyotensai.elf.server.config.ElfServerProperties;
import com.github.nikyotensai.elf.server.proxy.communicate.NettyServer;
import com.github.nikyotensai.elf.server.proxy.communicate.SessionManager;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnectionStore;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.command.CommunicateCommandStore;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.HostsValidatorHandler;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.RequestDecoder;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.TabHandler;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.UiRequestHandler;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.WebSocketEncoder;
import com.github.nikyotensai.elf.server.proxy.generator.IdGenerator;
import com.github.nikyotensai.elf.server.proxy.util.AppCenterServerFinder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author zhenyu.nie created on 2019 2019/5/16 11:33
 */
public class NettyServerForUi implements NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerForUi.class);

    private static final EventLoopGroup BOSS = new NioEventLoopGroup(1, new ThreadFactoryBuilder().setNameFormat("ui-netty-server-boss").build());

    private static final EventLoopGroup WORKER = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder().setNameFormat("ui-netty-server-worker").build());

    private final int port;

    private final IdGenerator idGenerator;

    private final UiConnectionStore uiConnectionStore;

    private final AgentConnectionStore agentConnectionStore;

    private final SessionManager sessionManager;

    private final CommunicateCommandStore commandStore;

    private final AppServerService appServerService;

    private final AppCenterServerFinder serverFinder;

    private volatile Channel channel;

    public NettyServerForUi(ElfServerProperties.ServerConf conf,
                            IdGenerator idGenerator,
                            CommunicateCommandStore commandStore,
                            UiConnectionStore uiConnectionStore,
                            AgentConnectionStore agentConnectionStore,
                            SessionManager sessionManager, AppServerService appServerService) {
        this.port = conf.getPort4Ui();
        this.idGenerator = idGenerator;
        this.uiConnectionStore = uiConnectionStore;
        this.agentConnectionStore = agentConnectionStore;
        this.sessionManager = sessionManager;
        this.commandStore = commandStore;
        this.appServerService = appServerService;
        this.serverFinder = new AppCenterServerFinder(this.appServerService);
    }

    @Override
    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)
                .channel(NioServerSocketChannel.class)
                .group(BOSS, WORKER)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pip = ch.pipeline();
                        pip.addLast(new IdleStateHandler(0, 0, 30 * 60 * 1000))
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(1024 * 1024))
                                .addLast(new WebSocketServerProtocolHandler("/ws"))
                                .addLast(new WebSocketFrameAggregator(1024 * 1024 * 1024))
                                .addLast(new RequestDecoder(new DefaultRequestEncryption(new RSAEncryption())))
                                .addLast(new WebSocketEncoder())
                                .addLast(new TabHandler())
                                .addLast(new HostsValidatorHandler(serverFinder))
                                .addLast(new UiRequestHandler(
                                        commandStore,
                                        uiConnectionStore,
                                        agentConnectionStore,
                                        sessionManager));
                    }
                });
        try {
            this.channel = bootstrap.bind(port).sync().channel();
            logger.info("client server startup successfully, port {}", port);
        } catch (Exception e) {
            logger.error("netty server for ui start fail", e);
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    public void stop() {
        try {
            BOSS.shutdownGracefully().sync();
            WORKER.shutdownGracefully().sync();
            channel.close();
        } catch (InterruptedException e) {
            logger.error("ui server close error", e);
        }
    }
}
