package com.github.nikyotensai.elf.attach.arthas.server;

import com.github.nikyotensai.elf.common.ElfConstants;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * @author leix.xie
 * @since 2019/9/23 19:31
 */
@Name(ElfConstants.STOP_COMMAND)
@Summary("Stop/Shutdown Arthas server and exit the console. Alias for shutdown.")
public class QStopCommand extends AnnotatedCommand {
    @Override
    public void process(CommandProcess process) {
        QShutdownCommand.shutdown(process);
    }
}