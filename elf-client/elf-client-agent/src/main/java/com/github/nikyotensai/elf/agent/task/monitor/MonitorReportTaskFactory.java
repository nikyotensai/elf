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

package com.github.nikyotensai.elf.agent.task.monitor;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.nikyotensai.elf.agent.common.task.AgentGlobalTaskFactory;
import com.github.nikyotensai.elf.common.NamedThreadFactory;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * @author leix.xie
 * @since 2019/1/8 18:09
 */
public class MonitorReportTaskFactory implements AgentGlobalTaskFactory {

    private static final ListeningScheduledExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("qmonitor-report-task", true)));

    @Override
    public void start() {
        TaskRunner taskRunner = new TaskRunner();
        executor.scheduleAtFixedRate(taskRunner, 0, 1, TimeUnit.MINUTES);
    }
}
