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

package com.github.nikyotensai.elf.commands;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.client.common.meta.MetaStore;
import com.github.nikyotensai.elf.client.common.meta.MetaStores;
import com.github.nikyotensai.elf.common.JacksonSerializer;
import com.github.nikyotensai.elf.remoting.netty.Processor;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingHeader;
import com.google.common.collect.ImmutableList;

/**
 * @author zhenyu.nie created on 2019 2019/1/10 15:19
 */
public class MetaRefreshProcessor implements Processor<String> {

    private static final Logger logger = LoggerFactory.getLogger(MetaRefreshProcessor.class);

    private final MetaStore metaStore = MetaStores.getMetaStore();

    @Override
    public List<Integer> types() {
        return ImmutableList.of(CommandCode.REQ_TYPE_REFRESH_AGENT_INFO.getCode());
    }

    @Override
    public void process(RemotingHeader header, String command, ResponseHandler handler) {
        try {
            Map<String, String> agentInfo = JacksonSerializer.deSerialize(command, new TypeReference<Map<String, String>>() {
            });
            if (agentInfo != null && agentInfo.size() > 0) {
                logger.info("meta refresh data receive, {}", agentInfo);
                metaStore.update(agentInfo);
            }
        } catch (Exception e) {
            logger.error("update meta store error", e);
        }
    }
}
