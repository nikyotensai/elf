package com.github.nikyotensai.elf.remoting.netty;

import java.util.List;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.agent.common.job.ResponseJobStore;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingHeader;
import com.google.common.collect.ImmutableList;

/**
 * @author zhenyu.nie created on 2019 2019/10/31 14:53
 */
public class JobResumeProcessor implements Processor<String> {

    private final ResponseJobStore jobStore;

    public JobResumeProcessor(ResponseJobStore jobStore) {
        this.jobStore = jobStore;
    }

    @Override
    public List<Integer> types() {
        return ImmutableList.of(CommandCode.REQ_TYPE_JOB_RESUME.getCode());
    }

    @Override
    public void process(RemotingHeader header, String command, ResponseHandler handler) {
        jobStore.resume(command);
    }
}
