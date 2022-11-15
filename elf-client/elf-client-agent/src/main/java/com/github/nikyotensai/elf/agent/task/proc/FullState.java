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

package com.github.nikyotensai.elf.agent.task.proc;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * @author cai.wen
 * @since 19-1-18
 */
public class FullState {

    public final CpuState cpuState;
    public final ProcessState processState;
    public final Map<Integer, ThreadState> threadInfo;

    public FullState(CpuState cpuState, ProcessState processState, Map<Integer, ThreadState> threadInfo) {
        this.cpuState = cpuState;
        this.processState = processState;
        this.threadInfo = ImmutableMap.copyOf(threadInfo);
    }

    @Override
    public String toString() {
        return "FullState{" +
                "cpuState=" + cpuState +
                ", processState=" + processState +
                ", threadInfo=" + threadInfo +
                '}';
    }
}
