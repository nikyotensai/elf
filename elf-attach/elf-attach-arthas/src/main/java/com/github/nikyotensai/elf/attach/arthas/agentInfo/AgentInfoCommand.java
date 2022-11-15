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

package com.github.nikyotensai.elf.attach.arthas.agentInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nikyotensai.elf.attach.arthas.util.AgentConfig;
import com.github.nikyotensai.elf.attach.common.AttachJacksonSerializer;
import com.github.nikyotensai.elf.attach.common.ElfLoggger;
import com.github.nikyotensai.elf.client.common.meta.MetaStores;
import com.github.nikyotensai.elf.common.URLCoder;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.logger.Logger;

import java.util.Map;

import static com.github.nikyotensai.elf.common.ElfConstants.REQ_AGENT_INFO;

/**
 * @author leix.xie
 * @since 2019/2/18 14:56
 */
@Name(REQ_AGENT_INFO)
public class AgentInfoCommand extends AnnotatedCommand {

    private static final Logger logger = ElfLoggger.getLogger();

    private static final AgentConfig config = new AgentConfig(MetaStores.getMetaStore());

    private String agentInfo;

    @Argument(index = 0, argName = "agentInfo")
    public void setAgentInfo(final String agentInfo) {
        this.agentInfo = URLCoder.decode(agentInfo);
    }

    @Override
    public void process(CommandProcess process) {
        try {

            Map<String, String> info = AttachJacksonSerializer.deSerialize(this.agentInfo, new TypeReference<Map<String, String>>() {
            });
            logger.debug("receive agent info update: {}", info);
            if (config.update(info)) {
                logger.info("update agent info: {}", info);
            }
        } catch (Throwable e) {
            logger.error("-1", "update meta info error", e);
        } finally {
            process.end();
        }
    }
}
