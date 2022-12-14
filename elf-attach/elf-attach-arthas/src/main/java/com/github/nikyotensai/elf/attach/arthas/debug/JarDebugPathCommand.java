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
 * @author leix.xie
 * @since 2019/3/6 17:48
 */
@Name(ElfConstants.REQ_JAR_CLASS_PATH)
public class JarDebugPathCommand extends AnnotatedCommand {
    private static final Logger logger = ElfLoggger.getLogger();

    private String className;

    @Argument(index = 0, argName = "className")
    public void setClassName(final String className) {
        this.className = URLCoder.decode(className);
    }

    @Override
    public void process(CommandProcess process) {
        logger.info("receive jar class path command, className: " + className);
        CodeProcessResponse<ClassInfo> codeResponse = new CodeProcessResponse<>();
        TypeResponse<ClassInfo> typeResponse = new TypeResponse<>();
        typeResponse.setType(ElfConstants.REQ_JAR_CLASS_PATH);
        typeResponse.setData(codeResponse);
        try {
            JarDebugClient client = JarDebugClients.getInstance();
            ClassInfo classInfo = client.getClassPath(className);

            codeResponse.setCode(0);
            codeResponse.setId(className);
            codeResponse.setData(classInfo);
        } catch (Exception e) {
            logger.error("", "get jar class path error, className: " + className, e);
            codeResponse.setCode(-1);
            codeResponse.setMessage("????????????????????????" + e.getMessage());
        } finally {
            process.write(URLCoder.encode(AttachJacksonSerializer.serialize(typeResponse)));
            process.end();
        }
    }
}
