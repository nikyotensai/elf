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

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.agent.common.cpujstack.KvUtils;
import com.github.nikyotensai.elf.agent.common.job.BytesJob;
import com.github.nikyotensai.elf.agent.common.job.ContinueResponseJob;
import com.github.nikyotensai.elf.agent.common.kv.KvDb;
import com.github.nikyotensai.elf.agent.common.util.DateUtils;
import com.github.nikyotensai.elf.common.JacksonSerializer;
import com.github.nikyotensai.elf.remoting.netty.AgentRemotingExecutor;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author zhenyu.nie created on 2019 2019/1/9 17:39
 */
public class CpuTimeTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(CpuTimeTask.class);

    private final SettableFuture<Integer> future = SettableFuture.create();

    private final String id;

    private final long maxRunningMs;

    private final KvDb kvDb;

    private final String threadId;

    private final DateTime start;

    private final DateTime end;

    private final ResponseHandler handler;

    public CpuTimeTask(String id, long maxRunningMs, KvDb kvDb, String threadId, DateTime start, DateTime end, ResponseHandler handler) {
        this.id = id;
        this.maxRunningMs = maxRunningMs;
        this.kvDb = kvDb;
        this.threadId = threadId;
        this.start = start;
        this.end = end;
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
        protected byte[] getBytes() throws Exception {
            List<CpuTime> cpuTimes = Lists.newArrayList();

            DateTime time = start;
            while (!time.isAfter(end)) {
                String timestamp = DateUtils.TIME_FORMATTER.print(time);
                int cpuTime = getCpuTime(timestamp);
                if (cpuTime > 0) {
                    cpuTimes.add(new CpuTime(timestamp, cpuTime));
                }
                time = time.plusMinutes(1);
            }

            Map<String, Object> map = Maps.newHashMap();

            map.put("type", "cpuTime");
            if (!Strings.isNullOrEmpty(threadId)) {
                map.put("threadId", threadId);
            }
            map.put("cpuTimes", cpuTimes);
            map.put("start", DateUtils.TIME_FORMATTER.print(start));
            map.put("end", DateUtils.TIME_FORMATTER.print(end));
            return JacksonSerializer.serializeToBytes(map);
        }

        @Override
        public ListeningExecutorService getExecutor() {
            return AgentRemotingExecutor.getExecutor();
        }
    }

    private int getCpuTime(String timestamp) {
        String key = KvUtils.getThreadMinuteCpuTimeKey(timestamp, threadId);
        String value = kvDb.get(key);
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    private static class CpuTime {

        private String timestamp;

        private double time;

        public CpuTime(String timestamp, int time) {
            this.timestamp = timestamp;
            this.time = ((double) time) / 100;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public double getTime() {
            return time;
        }

        @Override
        public String toString() {
            return "CpuTime{" +
                    "maxRunningMs='" + timestamp + '\'' +
                    ", time=" + time +
                    '}';
        }
    }
}
