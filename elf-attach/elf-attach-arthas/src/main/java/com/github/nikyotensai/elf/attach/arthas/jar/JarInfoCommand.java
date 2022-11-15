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

package com.github.nikyotensai.elf.attach.arthas.jar;

import com.github.nikyotensai.elf.attach.common.AttachJacksonSerializer;
import com.github.nikyotensai.elf.attach.common.ElfLoggger;
import com.github.nikyotensai.elf.common.CodeProcessResponse;
import com.github.nikyotensai.elf.common.ElfConstants;
import com.github.nikyotensai.elf.common.TypeResponse;
import com.github.nikyotensai.elf.common.URLCoder;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.logger.Logger;

import java.util.List;

/**
 * @author leix.xie
 * @since 2019/2/12 17:43
 */
@Name(ElfConstants.REQ_JAR_INFO)
public class JarInfoCommand extends AnnotatedCommand {
    private static final Logger logger = ElfLoggger.getLogger();

    @Override
    public void process(CommandProcess process) {
        logger.info("receive jar info command");
        CodeProcessResponse<List<String>> response = new CodeProcessResponse<>();
        TypeResponse<List<String>> typeResponse = new TypeResponse<>();
        typeResponse.setType(ElfConstants.REQ_JAR_INFO);
        typeResponse.setData(response);
        try {
            final JarInfoClient client = JarInfoClients.getInstance();
            response.setData(client.jarInfo());
            response.setCode(0);
        } catch (Throwable e) {
            logger.error("jar info error, {}", e.getMessage(), e);
            response.setCode(-1);
            response.setMessage("jar info error: " + e.getMessage());
        } finally {
            process.write(URLCoder.encode(AttachJacksonSerializer.serialize(typeResponse)));
            process.end();
        }
    }
}
