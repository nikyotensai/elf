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
import com.google.common.base.Strings;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.logger.Logger;

import java.util.Set;

/**
 * @author leix.xie
 * @since 2019/2/27 17:30
 */
@Name(ElfConstants.REQ_JAR_DEBUG)
public class JarDebugCommand extends AnnotatedCommand {
    private static final Logger logger = ElfLoggger.getLogger();

    private static final String RELOAD_ALL = "all";
    private String reload;

    @Option(shortName = "r", longName = "reload")
    public void setReload(String reload) {
        this.reload = reload;
    }

    @Override
    public void process(CommandProcess process) {
        logger.info("", "receive jar debug command, reload: {}", reload);
        CodeProcessResponse<Set<String>> codeResponse = new CodeProcessResponse<>();
        TypeResponse<Set<String>> typeResponse = new TypeResponse<>();
        typeResponse.setType(ElfConstants.REQ_JAR_DEBUG);
        typeResponse.setData(codeResponse);
        try {
            JarDebugClient client = JarDebugClients.getInstance();

            if (!Strings.isNullOrEmpty(reload)) {
                boolean success = reloadClasses(client);
                if (success) {
                    logger.info("", "reload class success, reload: {}", reload);
                }
            }

            Set<String> classPaths = client.getAllClass();
            codeResponse.setCode(0);
            codeResponse.setData(classPaths);
        } catch (Exception e) {
            logger.error("", "get jar debug info error", e);
            codeResponse.setCode(-1);
            codeResponse.setMessage("获取类列表失败，" + e.getMessage());
        } finally {
            logger.info("", "finish jar debug command, code :{}", codeResponse.getCode());
            process.write(URLCoder.encode(AttachJacksonSerializer.serialize(typeResponse)));
            process.end();
        }
    }

    private boolean reloadClasses(JarDebugClient client) {
        try {
            if (RELOAD_ALL.equalsIgnoreCase(reload)) {
                return client.reloadAllClass();
            } else {
                return client.reLoadNewClass();
            }
        } catch (Throwable t) {
            logger.error("", "reload classes error", t);
            return false;
        }
    }
}
