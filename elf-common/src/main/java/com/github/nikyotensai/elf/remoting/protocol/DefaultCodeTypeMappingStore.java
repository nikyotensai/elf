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

package com.github.nikyotensai.elf.remoting.protocol;

import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_ARTHAS;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_CANCEL;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_CONFIG;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_CPU_JSTACK_THREADS;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_CPU_JSTACK_TIMES;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_CPU_THREAD_NUM;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_DEBUG;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_DECOMPILER;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_DOWNLOAD_FILE;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_HOST_HEAP_HISTO;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_HOST_JVM;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_HOST_THREAD;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_JAR_DEBUG;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_JAR_INFO;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_JAVA;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_JOB_PAUSE;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_JOB_RESUME;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_LIST_DOWNLOAD_FILE;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_MONITOR;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_PROFILER_ALL_FILE_END;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_PROFILER_FILE;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_PROFILER_FILE_END;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_PROFILER_FILE_ERROR;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_PROFILER_INFO;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_PROFILER_START;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_PROFILER_STATE_SEARCH;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_PROFILER_STOP;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_QMONITOR_QUERY;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_REFRESH_AGENT_INFO;
import static com.github.nikyotensai.elf.remoting.protocol.CommandCode.REQ_TYPE_REFRESH_TIP;

import java.util.Map;
import java.util.Set;

import com.github.nikyotensai.elf.remoting.command.CpuTimeCommand;
import com.github.nikyotensai.elf.remoting.command.DecompilerCommand;
import com.github.nikyotensai.elf.remoting.command.DownloadCommand;
import com.github.nikyotensai.elf.remoting.command.HeapHistoCommand;
import com.github.nikyotensai.elf.remoting.command.MachineCommand;
import com.github.nikyotensai.elf.remoting.command.MonitorCommand;
import com.github.nikyotensai.elf.remoting.command.ThreadCommand;
import com.github.nikyotensai.elf.remoting.command.ThreadNumCommand;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * @author zhenyu.nie created on 2019 2019/5/23 20:04
 */
public class DefaultCodeTypeMappingStore implements CodeTypeMappingStore {

    private final Map<Integer, Class<?>> codeTypeMappings = Maps.newHashMap();

    DefaultCodeTypeMappingStore() {
        final Class stringClass = String.class;

        //arthas
        register(stringClass, ImmutableSet.of(REQ_TYPE_ARTHAS.getCode(),
                REQ_TYPE_DEBUG.getCode(),
                REQ_TYPE_MONITOR.getCode(),
                REQ_TYPE_JAR_INFO.getCode(),
                REQ_TYPE_CONFIG.getCode(),
                REQ_TYPE_JAR_DEBUG.getCode(),
                REQ_TYPE_PROFILER_STOP.getCode(),
                REQ_TYPE_PROFILER_START.getCode(),
                REQ_TYPE_PROFILER_STATE_SEARCH.getCode(),
                REQ_TYPE_PROFILER_INFO.getCode()));

        //线程级cpu监控
        register(CpuTimeCommand.class, ImmutableSet.of(REQ_TYPE_CPU_JSTACK_TIMES.getCode()));
        register(stringClass, ImmutableSet.of(REQ_TYPE_CPU_JSTACK_THREADS.getCode()));
        register(ThreadNumCommand.class, ImmutableSet.of(REQ_TYPE_CPU_THREAD_NUM.getCode()));

        //decompiler
        register(DecompilerCommand.class, ImmutableSet.of(REQ_TYPE_DECOMPILER.getCode()));
        //HeapHisto
        register(HeapHistoCommand.class, ImmutableSet.of(REQ_TYPE_HOST_HEAP_HISTO.getCode()));
        //host
        register(Integer.class, ImmutableSet.of(REQ_TYPE_HOST_JVM.getCode()));
        //thread info
        register(ThreadCommand.class, ImmutableSet.of(REQ_TYPE_HOST_THREAD.getCode()));
        //monitor query
        register(MonitorCommand.class, ImmutableSet.of(REQ_TYPE_QMONITOR_QUERY.getCode()));
        //heartbeat
        register(stringClass, ImmutableSet.of(ResponseCode.RESP_TYPE_HEARTBEAT.getCode()));
        //linux command process
        register(MachineCommand.class, ImmutableSet.of(CommandCode.REQ_TYPE_COMMAND.getCode()));
        //java command process
        register(MachineCommand.class, ImmutableSet.of(REQ_TYPE_JAVA.getCode()));
        //QJTools processor
        register(MachineCommand.class, ImmutableSet.of(CommandCode.REQ_TYPE_QJTOOLS.getCode()));
        //meta refresh
        register(stringClass, ImmutableSet.of(REQ_TYPE_REFRESH_AGENT_INFO.getCode()));
        //meta refresh tip
        register(stringClass, ImmutableSet.of(REQ_TYPE_REFRESH_TIP.getCode()));
        //cancel
        register(stringClass, ImmutableSet.of(REQ_TYPE_CANCEL.getCode()));
        //job pause
        register(stringClass, ImmutableSet.of(REQ_TYPE_JOB_PAUSE.getCode()));
        //job resume
        register(stringClass, ImmutableSet.of(REQ_TYPE_JOB_RESUME.getCode()));
        //download file list
        register(stringClass, ImmutableSet.of(REQ_TYPE_LIST_DOWNLOAD_FILE.getCode()));
        //download file
        register(DownloadCommand.class, ImmutableSet.of(REQ_TYPE_DOWNLOAD_FILE.getCode()));

        //profiler file
        register(stringClass, ImmutableSet.of(REQ_TYPE_PROFILER_FILE.getCode()));
        register(stringClass, ImmutableSet.of(REQ_TYPE_PROFILER_FILE_END.getCode()));
        register(stringClass, ImmutableSet.of(REQ_TYPE_PROFILER_FILE_ERROR.getCode()));
        register(stringClass, ImmutableSet.of(REQ_TYPE_PROFILER_ALL_FILE_END.getCode()));
    }

    public void register(Class<?> type, Set<Integer> codes) {
        for (Integer code : codes) {
            codeTypeMappings.put(code, type);
        }
    }

    @Override
    public Class<?> getMappingType(int code) {
        return codeTypeMappings.get(code);
    }
}
