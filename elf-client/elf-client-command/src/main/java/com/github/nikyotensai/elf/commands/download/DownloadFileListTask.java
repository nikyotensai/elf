package com.github.nikyotensai.elf.commands.download;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.agent.common.job.BytesJob;
import com.github.nikyotensai.elf.agent.common.job.ContinueResponseJob;
import com.github.nikyotensai.elf.common.CodeProcessResponse;
import com.github.nikyotensai.elf.common.FileUtil;
import com.github.nikyotensai.elf.common.JacksonSerializer;
import com.github.nikyotensai.elf.common.TypeResponse;
import com.github.nikyotensai.elf.remoting.netty.AgentRemotingExecutor;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author leix.xie
 * @since 2019/11/4 16:32
 */
public class DownloadFileListTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(DownloadFileListTask.class);

    private final String id;

    private final ResponseHandler handler;

    private final long maxRunningMs;

    private final List<String> filePath;

    private final SettableFuture<Integer> future = SettableFuture.create();

    private static final Splitter FILE_PATH_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

    public DownloadFileListTask(String id, String command, ResponseHandler handler, long maxRunningMs) {
        this.id = id;
        this.handler = handler;
        this.maxRunningMs = maxRunningMs;
        filePath = FILE_PATH_SPLITTER.splitToList(Strings.nullToEmpty(command).trim());
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

        protected Job() {
            super(id, handler, future);
        }

        @Override
        protected byte[] getBytes() throws Exception {
            TypeResponse<List<DownloadFileBean>> typeResponse = new TypeResponse<>();
            CodeProcessResponse<List<DownloadFileBean>> response = new CodeProcessResponse<>();
            typeResponse.setData(response);
            typeResponse.setType("downloadfilelist");

            List<DownloadFileBean> result = Lists.newArrayList();

            if (filePath != null && !filePath.isEmpty()) {
                listFile(result);
            }
            response.setCode(0);
            response.setData(result);
            return JacksonSerializer.serializeToBytes(typeResponse);
        }


        private void listFile(List<DownloadFileBean> result) {
            for (String path : filePath) {
                List<File> files = doListFile(path);
                List<DownloadFileBean> fileBeans = Lists.transform(files, new Function<File, DownloadFileBean>() {
                    @Override
                    public DownloadFileBean apply(File file) {
                        return new DownloadFileBean(file.getName(), file.getAbsolutePath(), file.length(), file.lastModified());
                    }
                });
                result.addAll(fileBeans);
            }
        }

        private List<File> doListFile(final String path) {
            return FileUtil.listFile(new File(path), Predicates.<File>alwaysTrue());
        }

        @Override
        public ListeningExecutorService getExecutor() {
            return AgentRemotingExecutor.getExecutor();
        }
    }
}
