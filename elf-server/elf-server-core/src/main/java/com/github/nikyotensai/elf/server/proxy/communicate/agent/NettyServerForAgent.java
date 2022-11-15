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

package com.github.nikyotensai.elf.server.proxy.communicate.agent;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.common.Throwables;
import com.github.nikyotensai.elf.remoting.coder.AgentDecoder;
import com.github.nikyotensai.elf.remoting.coder.AgentEncoder;
import com.github.nikyotensai.elf.server.config.ElfServerProperties.ServerConf;
import com.github.nikyotensai.elf.server.proxy.communicate.NettyServer;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.handler.AgentMessageHandler;
import com.github.nikyotensai.elf.server.proxy.communicate.handle.ChannelCloseHandler;
import com.github.nikyotensai.elf.server.proxy.communicate.handle.ConnectionCounterHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author zhenyu.nie created on 2019 2019/5/13 18:54
 */
public class NettyServerForAgent implements NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerForAgent.class);

    private final EventLoopGroup BOSS_GROUP = new NioEventLoopGroup(1, new ThreadFactoryBuilder().setNameFormat("agent-netty-server-boss").build());

    private final EventLoopGroup WORKER_GROUP = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder().setNameFormat("agent-netty-server-worker").build());

    private final int heartbeatTimeoutSec;

    private final AgentMessageHandler agentMessageHandler;

    private final int port;

    private volatile Channel channel;

    public NettyServerForAgent(ServerConf conf, AgentMessageHandler agentMessageHandler) {
        int heartbeatSec = conf.getHeartbeatSec();
        this.heartbeatTimeoutSec = heartbeatSec * 2 + heartbeatSec / 2;
        this.agentMessageHandler = agentMessageHandler;
        this.port = conf.getPort4Agent();
    }

    @Override
    public void start() {
        ConnectionCounterHandler connectionCounterHandler = new ConnectionCounterHandler("agent");
        ServerBootstrap bootstrap = new ServerBootstrap()
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)
                .group(BOSS_GROUP, WORKER_GROUP)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("connectionCounter", connectionCounterHandler)
                                .addLast("encoder", new AgentEncoder())
                                .addLast("decoder", new AgentDecoder())
                                .addLast("idleHandler", new IdleStateHandler(heartbeatTimeoutSec, 0, 0, TimeUnit.SECONDS))
                                .addLast("messageHandler", agentMessageHandler)
                                .addLast("closeHandler", new ChannelCloseHandler("agent"));
                    }
                });

        try {
            this.channel = bootstrap.bind(port).sync().channel();
            logger.info("netty server for agent, port {}", port);
        } catch (Exception e) {
            logger.error("netty server for agent start fail", e);
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
            BOSS_GROUP.shutdownGracefully().sync();
            WORKER_GROUP.shutdownGracefully().sync();
            channel.close();
        } catch (InterruptedException e) {
            logger.error("agent client close error", e);
        }
    }
}
