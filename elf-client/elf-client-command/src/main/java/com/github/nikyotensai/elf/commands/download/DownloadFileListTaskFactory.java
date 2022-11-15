package com.github.nikyotensai.elf.commands.download;

import java.util.Set;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.github.nikyotensai.elf.remoting.netty.TaskFactory;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingHeader;
import com.google.common.collect.ImmutableSet;

/**
 * @author leix.xie
 * @since 2019/11/4 16:23
 */
public class DownloadFileListTaskFactory implements TaskFactory<String> {

    @Override
    public Set<Integer> codes() {
        return ImmutableSet.of(CommandCode.REQ_TYPE_LIST_DOWNLOAD_FILE.getCode());
    }

    @Override
    public String name() {
        return "list download file";
    }

    @Override
    public Task create(RemotingHeader header, String command, ResponseHandler handler) {
        return new DownloadFileListTask(header.getId(), command, handler, header.getMaxRunningMs());
    }
}
