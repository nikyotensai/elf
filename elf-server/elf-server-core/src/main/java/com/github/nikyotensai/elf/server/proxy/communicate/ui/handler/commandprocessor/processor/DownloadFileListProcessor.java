package com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.commandprocessor.processor;

import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RequestData;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.commandprocessor.AbstractCommand;
import com.github.nikyotensai.elf.server.proxy.util.DownloadDirUtils;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author leix.xie
 * @since 2019/11/4 17:02
 */
@Service
public class DownloadFileListProcessor extends AbstractCommand<String> {


    @PostConstruct
    public void init() {

    }

    @Override
    public Set<Integer> getCodes() {
        return ImmutableSet.of(CommandCode.REQ_TYPE_LIST_DOWNLOAD_FILE.getCode());
    }

    @Override
    protected Optional<RequestData<String>> doPreprocessor(RequestData<String> requestData, ChannelHandlerContext ctx) {
        String command = Strings.nullToEmpty(requestData.getCommand()).trim();
        String newCommand = DownloadDirUtils.composeDownloadDir(requestData.getApp(), requestData.getAgentServerInfos(), command);
        if (Strings.isNullOrEmpty(newCommand)) {
            throw new RuntimeException("No folders to download");
        }
        requestData.setCommand(newCommand);
        return Optional.of(requestData);
    }

    @Override
    public int getMinAgentVersion() {
        return 11;
    }

    @Override
    public boolean supportMulti() {
        return false;
    }
}
