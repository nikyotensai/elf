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

import java.util.concurrent.Executors;

import com.github.nikyotensai.elf.common.ElfClientConfig;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * @author zhenyu.nie created on 2018 2018/10/9 15:04
 */
public class AgentRemotingExecutor {

    private static final ListeningExecutorService executorService;

    static {
        int threadNum = Integer.parseInt(ElfClientConfig.ELF_AGENT_THREAD_NUM);
        executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(threadNum, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("log-agent-exec-%d").build()));
    }

    public static ListeningExecutorService getExecutor() {
        return executorService;
    }
}
