package com.github.nikyotensai.elf.commands.profiler;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.github.nikyotensai.elf.remoting.netty.TaskFactory;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingHeader;
import com.google.common.collect.ImmutableSet;

/**
 * @author cai.wen created on 19-12-11 下午4:52
 */
public class ProfilerFileForProxyTaskFactory implements TaskFactory<String> {

    private static final Logger logger = LoggerFactory.getLogger(ProfilerFileForProxyTaskFactory.class);

    private static final String NAME = "profilerFile";

    @Override
    public Set<Integer> codes() {
        return ImmutableSet.of(CommandCode.REQ_TYPE_PROFILER_FILE.getCode());
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Task create(RemotingHeader header, String command, ResponseHandler handler) {
        return new ProfilerFileForProxyTask(header.getId(), header.getMaxRunningMs(), handler, command);
    }
}
