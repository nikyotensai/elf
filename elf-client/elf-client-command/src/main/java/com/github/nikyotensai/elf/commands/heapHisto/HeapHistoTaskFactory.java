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

package com.github.nikyotensai.elf.commands.heapHisto;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.remoting.command.HeapHistoCommand;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.github.nikyotensai.elf.remoting.netty.TaskFactory;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingHeader;
import com.google.common.collect.ImmutableSet;

/**
 * @author leix.xie
 * @since 2018/12/10 14:35
 */
public class HeapHistoTaskFactory implements TaskFactory<HeapHistoCommand> {

    private static final Logger logger = LoggerFactory.getLogger(HeapHistoTaskFactory.class);

    private static final String NAME = "heapHisto";

    @Override
    public Set<Integer> codes() {
        return ImmutableSet.of(CommandCode.REQ_TYPE_HOST_HEAP_HISTO.getCode());
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Task create(RemotingHeader header, HeapHistoCommand command, ResponseHandler handler) {
        final String param = command.getParam();
        final long selectTimestamp = command.getTimestamp();
        int pid = Integer.valueOf(command.getPid());
        logger.info("get heap histo command: {}", command);
        HeapHistoTask task = new HeapHistoTask(header.getId(), pid, selectTimestamp, param, handler, header.getMaxRunningMs());
        return task;
    }
}
