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

package com.github.nikyotensai.elf.agent.task.cpujstack;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.nikyotensai.elf.agent.common.config.AgentConfig;
import com.github.nikyotensai.elf.agent.common.kv.KvDb;
import com.github.nikyotensai.elf.agent.common.kv.KvDbs;
import com.github.nikyotensai.elf.agent.common.task.AgentGlobalTaskFactory;
import com.github.nikyotensai.elf.client.common.meta.MetaStores;
import com.github.nikyotensai.elf.common.NamedThreadFactory;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * @author zhenyu.nie created on 2019 2019/1/8 17:29
 */
public class CpuJStackTaskFactory implements AgentGlobalTaskFactory {

    private static final ListeningScheduledExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("cpu-jstack-task", true)));

    private static final KvDb kvDb = KvDbs.getKvDb();

    private static final AgentConfig agentConfig = new AgentConfig(MetaStores.getMetaStore());

    @Override
    public void start() {
        PidExecutor jstackExecutor = new JStackPidExecutor();
        PidRecordExecutor momentCpuTimePidExecutor = new MomentCpuTimeRecordExecutor(executor);
        TaskRunner taskRunner = new TaskRunner(agentConfig, kvDb, jstackExecutor, momentCpuTimePidExecutor);
        executor.scheduleAtFixedRate(taskRunner, 5, 60, TimeUnit.SECONDS);
    }
}
