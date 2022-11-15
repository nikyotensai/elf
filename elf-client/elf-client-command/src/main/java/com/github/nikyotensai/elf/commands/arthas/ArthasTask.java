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

package com.github.nikyotensai.elf.commands.arthas;

import java.util.Set;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.agent.common.job.ContinueResponseJob;
import com.github.nikyotensai.elf.commands.arthas.telnet.Telnet;
import com.github.nikyotensai.elf.commands.arthas.telnet.TelnetStore;
import com.github.nikyotensai.elf.common.ElfConstants;
import com.github.nikyotensai.elf.common.NamedThreadFactory;
import com.github.nikyotensai.elf.remoting.netty.AgentRemotingExecutor;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author zhenyu.nie created on 2018 2018/10/15 18:55
 */
public class ArthasTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(ArthasTask.class);

    private static final ListeningExecutorService SHUTDOWN_EXECUTOR = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor(new NamedThreadFactory("shutdown_attach")));

    private static final Set<String> SHUTDOWN_COMMANDS = ImmutableSet.of(ElfConstants.SHUTDOWN_COMMAND, ElfConstants.STOP_COMMAND);

    private final TelnetStore telnetStore;

    private final String id;

    private final long maxRunningMs;

    private final int pid;

    private final String command;

    private final ResponseHandler handler;

    private final SettableFuture<Integer> future = SettableFuture.create();

    public ArthasTask(TelnetStore telnetStore, String id, long maxRunningMs, int pid, String command, ResponseHandler handler) {
        this.telnetStore = telnetStore;
        this.id = id;
        this.maxRunningMs = maxRunningMs;
        this.pid = pid;
        this.command = command;
        this.handler = handler;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getMaxRunningMs() {
        return maxRunningMs;
    }

    @Override
    public ContinueResponseJob createJob() {
        if (isShutdownCommand(command.trim())) {
            return new Job(SHUTDOWN_EXECUTOR);
        } else {
            return new Job(AgentRemotingExecutor.getExecutor());
        }
    }

    @Override
    public ListenableFuture<Integer> getResultFuture() {
        return future;
    }

    private boolean isShutdownCommand(String realCommand) {
        return SHUTDOWN_COMMANDS.contains(realCommand);
    }

    private class Job implements ContinueResponseJob {

        private final ListeningExecutorService executor;

        private Telnet telnet;

        private Job(ListeningExecutorService executor) {
            this.executor = executor;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void init() throws Exception {
            telnet = telnetStore.getTelnet(pid);
            telnet.write(command);
        }

        @Override
        public boolean doResponse() throws Exception {
            byte[] bytes = telnet.read();
            if (bytes == null) {
                return true;
            }

            if (bytes.length > 0) {
                handler.handle(bytes);
            }
            return false;
        }

        @Override
        public void clear() {
            if (telnet != null) {
                telnet.close();
            }
        }

        @Override
        public void finish() throws Exception {
            future.set(0);
        }

        @Override
        public void error(Throwable t) {
            future.setException(t);
        }

        @Override
        public void cancel() {
            future.cancel(true);
        }

        @Override
        public ListeningExecutorService getExecutor() {
            return executor;
        }
    }
}
