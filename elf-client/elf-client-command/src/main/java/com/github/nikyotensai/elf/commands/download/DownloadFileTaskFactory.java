package com.github.nikyotensai.elf.commands.download;

import java.util.Set;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.remoting.command.DownloadCommand;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.github.nikyotensai.elf.remoting.netty.TaskFactory;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingHeader;
import com.google.common.collect.ImmutableSet;

/**
 * @author leix.xie
 * @since 2019/11/5 15:43
 */
public class DownloadFileTaskFactory implements TaskFactory<DownloadCommand> {

    @Override
    public Set<Integer> codes() {
        return ImmutableSet.of(CommandCode.REQ_TYPE_DOWNLOAD_FILE.getCode());
    }

    @Override
    public String name() {
        return "download file";
    }

    @Override
    public Task create(RemotingHeader header, DownloadCommand command, ResponseHandler handler) {
        return new DownloadFileTask(header.getId(), header.getMaxRunningMs(), command, handler);
    }
}
