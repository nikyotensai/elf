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

package com.github.nikyotensai.elf.commands;

import java.io.File;
import java.io.InputStream;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.agent.common.ClosableProcess;
import com.github.nikyotensai.elf.agent.common.ClosableProcesses;
import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.agent.common.job.ContinueResponseJob;
import com.github.nikyotensai.elf.client.common.store.ElfStore;
import com.github.nikyotensai.elf.common.ElfConstants;
import com.github.nikyotensai.elf.common.FileUtil;
import com.github.nikyotensai.elf.remoting.netty.AgentRemotingExecutor;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author zhenyu.nie created on 2018 2018/10/9 12:12
 */
public class SystemTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(SystemTask.class);

    private static final String TIME_PATTERN = "yyyyMMddHHmmssSSS";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern(TIME_PATTERN);

    private final String id;

    private final ProcessBuilder processBuilder;

    private final ResponseHandler handler;

    private final long maxRunningMs;

    private final SettableFuture<Integer> future = SettableFuture.create();
    private static final String BASE_DUMP_DIR = ElfStore.getDumpFileStorePath();
    private static final String JSTACK_DUMP_DIR = FileUtil.dealPath(BASE_DUMP_DIR, "jstack");
    private boolean isJstack = false;
    private String jstackFileName;

    static {
        FileUtil.ensureDirectoryExists(JSTACK_DUMP_DIR);
    }

    public SystemTask(String id,
                      String command,
                      String presentWorkDir,
                      ResponseHandler handler,
                      long maxRunningMs) {
        this.id = id;
        String realCommand = CustomScript.replaceScriptPath(command);

        if (realCommand.contains(ElfConstants.FILL_DUMP_TARGET)) {
            isJstack = true;
            jstackFileName = JSTACK_DUMP_DIR + File.separator + "jstack-" + TIME_FORMATTER.print(System.currentTimeMillis()) + ".txt";
            realCommand = realCommand.replace(ElfConstants.FILL_DUMP_TARGET, " | tee " + jstackFileName);
        }

        this.processBuilder = new ProcessBuilder()
                .directory(new File(presentWorkDir)).redirectErrorStream(true).command("/bin/bash", "-c", realCommand);
        this.handler = handler;
        this.maxRunningMs = maxRunningMs;
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

    private class Job implements ContinueResponseJob {

        private ClosableProcess process;

        private InputStream inputStream;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void init() throws Exception {
            process = ClosableProcesses.wrap(processBuilder.start());
            inputStream = process.getInputStream();
        }

        @Override
        public boolean doResponse() throws Exception {
            byte[] bytes = process.read();
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
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable e) {
                    logger.error("close input stream error, {}", id);
                }
            }

            if (process != null) {
                try {
                    process.destroy();
                } catch (Throwable e) {
                    logger.error("close process error, {}", id);
                }
            }
        }

        @Override
        public void finish() throws Exception {
            int code = process.waitFor();
            if (isJstack) {
                handler.handle("\033[31m[??????]\033[0m ??????1?????????????????????????????????????????? "
                        + JSTACK_DUMP_DIR + " ???????????????jstack??????" +
                        "\n?????????" + jstackFileName +
                        "\n????????????????????????????????????????????????");
            }
            future.set(code);
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
            return AgentRemotingExecutor.getExecutor();
        }
    }
}
