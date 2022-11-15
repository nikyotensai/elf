package com.github.nikyotensai.elf.remoting.netty;

import java.util.List;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.agent.common.job.ResponseJobStore;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingHeader;
import com.google.common.collect.ImmutableList;

/**
 * @author zhenyu.nie created on 2019 2019/10/31 14:49
 */
public class JobPauseProcessor implements Processor<String> {

    private final ResponseJobStore jobStore;

    public JobPauseProcessor(ResponseJobStore jobStore) {
        this.jobStore = jobStore;
    }

    @Override
    public List<Integer> types() {
        return ImmutableList.of(CommandCode.REQ_TYPE_JOB_PAUSE.getCode());
    }

    @Override
    public void process(RemotingHeader header, String command, ResponseHandler handler) {
        jobStore.pause(command);
    }
}
