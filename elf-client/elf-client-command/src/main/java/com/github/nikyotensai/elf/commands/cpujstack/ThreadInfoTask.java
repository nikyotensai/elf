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

package com.github.nikyotensai.elf.commands.cpujstack;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.agent.common.cpujstack.KvUtils;
import com.github.nikyotensai.elf.agent.common.cpujstack.ThreadInfo;
import com.github.nikyotensai.elf.agent.common.job.BytesJob;
import com.github.nikyotensai.elf.agent.common.job.ContinueResponseJob;
import com.github.nikyotensai.elf.agent.common.kv.KvDb;
import com.github.nikyotensai.elf.common.JacksonSerializer;
import com.github.nikyotensai.elf.remoting.netty.AgentRemotingExecutor;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author zhenyu.nie created on 2019 2019/1/9 19:35
 */
public class ThreadInfoTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(ThreadInfoTask.class);

    private static final TypeReference<Map<String, ThreadInfo>> TYPE_REFERENCE = new TypeReference<Map<String, ThreadInfo>>() {
    };

    private final SettableFuture<Integer> future = SettableFuture.create();

    private final String id;

    private final long maxRunningMs;

    private final KvDb kvDb;

    private final ResponseHandler handler;

    private final String time;

    public ThreadInfoTask(String id, long maxRunningMs, KvDb kvDb, ResponseHandler handler, String time) {
        this.id = id;
        this.maxRunningMs = maxRunningMs;
        this.kvDb = kvDb;
        this.handler = handler;
        this.time = time;
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
        return new Job();
    }

    @Override
    public ListenableFuture<Integer> getResultFuture() {
        return future;
    }

    private class Job extends BytesJob {

        private Job() {
            super(id, handler, future);
        }

        @Override
        protected byte[] getBytes() {
            Map<String, Object> map = Maps.newHashMap();
            map.put("type", "jstackThreads");
            map.put("time", time);
            String threadInfoStr = kvDb.get(KvUtils.getThreadInfoKey(time));
            Map<String, ThreadInfo> threadInfo = Maps.newHashMap();
            if (!Strings.isNullOrEmpty(threadInfoStr)) {
                threadInfo = JacksonSerializer.deSerialize(threadInfoStr, TYPE_REFERENCE);
            }
            addMomentCpuTimeInfo(threadInfo, time);
            map.put("threadInfo", threadInfo);
            String jstack = kvDb.get(KvUtils.getJStackResultKey(time));
            map.put("jstack", Strings.nullToEmpty(jstack));

            return JacksonSerializer.serializeToBytes(map);
        }

        @Override
        public ListeningExecutorService getExecutor() {
            return AgentRemotingExecutor.getExecutor();
        }
    }

    private void addMomentCpuTimeInfo(Map<String, ThreadInfo> threadInfo, String time) {
        for (ThreadInfo info : threadInfo.values()) {
            String momentCpuTime = kvDb.get(KvUtils.getThreadMomentCpuTimeKey(time, info.getId()));
            if (momentCpuTime == null) {
                momentCpuTime = "0";
            }
            info.setCpuTime(Integer.parseInt(momentCpuTime));
        }
    }
}
