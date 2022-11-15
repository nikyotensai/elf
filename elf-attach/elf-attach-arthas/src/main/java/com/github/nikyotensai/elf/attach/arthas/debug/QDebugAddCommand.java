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
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.logger.Logger;

/**
 * @author zhenyu.nie created on 2018 2018/11/22 17:16
 */
@Name(ElfConstants.REQ_DEBUG_ADD)
public class QDebugAddCommand extends AnnotatedCommand {

    private static final Logger logger = ElfLoggger.getLogger();

    private String id;

    private String source;

    private int line;

    private String condition;

    @Argument(index = 0, argName = "id")
    public void setId(String id) {
        this.id = id;
    }

    @Argument(index = 1, argName = "source")
    public void setSource(String source) {
        this.source = URLCoder.decode(source);
    }

    @Argument(index = 2, argName = "line")
    public void setLine(int line) {
        this.line = line;
    }

    @Option(shortName = "c", longName = "condition")
    public void setCondition(String condition) {
        this.condition = URLCoder.decode(condition);
    }

    @Override
    public void process(CommandProcess process) {
        logger.info("receive debug command, source [{}], line [{}], condition [{}], id [{}]", (Object) source, line, condition, id);
        CodeProcessResponse<String> codeResponse = new CodeProcessResponse<>();
        TypeResponse<String> typeResponse = new TypeResponse<>();
        typeResponse.setType(ElfConstants.REQ_DEBUG_ADD);
        typeResponse.setData(codeResponse);
        try {
            QDebugClient debugClient = QDebugClients.getInstance();
            String breakPointId = debugClient.registerBreakpoint(source, line, condition);
            codeResponse.setId(id);
            codeResponse.setCode(0);
            codeResponse.setData(breakPointId);
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
