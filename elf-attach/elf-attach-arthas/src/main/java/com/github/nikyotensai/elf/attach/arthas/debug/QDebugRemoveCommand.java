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

package com.github.nikyotensai.elf.attach.arthas.debug;

import com.github.nikyotensai.elf.attach.common.AttachJacksonSerializer;
import com.github.nikyotensai.elf.attach.common.ElfLoggger;
import com.github.nikyotensai.elf.common.CodeProcessResponse;
import com.github.nikyotensai.elf.common.ElfConstants;
import com.github.nikyotensai.elf.common.TypeResponse;
import com.github.nikyotensai.elf.common.URLCoder;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.logger.Logger;

/**
 * @author zhenyu.nie created on 2018 2018/11/28 20:05
 */
@Name(ElfConstants.REQ_DEBUG_REMOVE)
public class QDebugRemoveCommand extends AnnotatedCommand {

    private static final Logger logger = ElfLoggger.getLogger();

    private String id;

    @Argument(index = 0, argName = "id")
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void process(CommandProcess process) {
        logger.info("receive remove command, id [{}]", (Object) id);
        CodeProcessResponse<String> codeResponse = new CodeProcessResponse<>();
        TypeResponse<String> typeResponse = new TypeResponse<>();
        typeResponse.setType(ElfConstants.REQ_DEBUG_REMOVE);
        typeResponse.setData(codeResponse);
        try {
            QDebugClient debugClient = QDebugClients.getInstance();
            debugClient.remoteBreakPoint(id);
            codeResponse.setId(id);
            codeResponse.setCode(0);
        } catch (Throwable e) {
            logger.error("qdebug-add-error", e.getMessage(), e);
            codeResponse.setId(id);
            codeResponse.setCode(-1);
            codeResponse.setMessage(e.getMessage());
        }

        process.write(URLCoder.encode(AttachJacksonSerializer.serialize(typeResponse)));
        process.end();
    }
}
