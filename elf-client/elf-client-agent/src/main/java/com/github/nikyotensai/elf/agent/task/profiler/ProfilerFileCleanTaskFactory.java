package com.github.nikyotensai.elf.agent.task.profiler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.nikyotensai.elf.agent.common.task.AgentGlobalTaskFactory;
import com.github.nikyotensai.elf.common.NamedThreadFactory;

/**
 * @author cai.wen created on 19-11-28 下午5:25
 */
public class ProfilerFileCleanTaskFactory implements AgentGlobalTaskFactory {

    private static final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("profiler-file-clean-task", true));

    @Override
    public void start() {
        executor.scheduleAtFixedRate(new TaskRunner(), 0, 1, TimeUnit.DAYS);
    }
}