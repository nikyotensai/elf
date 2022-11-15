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

package com.github.nikyotensai.elf.attach.arthas.config;

import com.github.nikyotensai.elf.attach.common.AttachJacksonSerializer;
import com.github.nikyotensai.elf.attach.common.ElfLoggger;
import com.github.nikyotensai.elf.attach.file.bean.FileBean;
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
 * @since 2019/3/5 10:28
 */
@Name(ElfConstants.REQ_APP_CONFIG)
public class AppConfigCommand extends AnnotatedCommand {
    private static final Logger logger = ElfLoggger.getLogger();

    @Override
    public void process(CommandProcess process) {
        logger.info("receive app config command");
        CodeProcessResponse<List<FileBean>> response = new CodeProcessResponse<>();
        TypeResponse<List<FileBean>> typeResponse = new TypeResponse<>();
        typeResponse.setType(ElfConstants.REQ_APP_CONFIG);
        typeResponse.setData(response);
        try {
            AppConfigClient client = AppConfigClients.getInstance();
            List<FileBean> config = getAppConfig(client);
            response.setCode(0);
            response.setData(config);
        } catch (Exception e) {
            logger.error("get app config error, {}", e.getMessage(), e);
            response.setCode(-1);
            response.setMessage("获取配置文件信息出错, " + e.getMessage());
        } finally {
            process.write(URLCoder.encode(AttachJacksonSerializer.serialize(typeResponse)));
            process.end();
        }
    }

    private List<FileBean> getAppConfig(final AppConfigClient client) {
        List<FileBean> files = client.listAppConfigFiles();
        return files;
    }
}
