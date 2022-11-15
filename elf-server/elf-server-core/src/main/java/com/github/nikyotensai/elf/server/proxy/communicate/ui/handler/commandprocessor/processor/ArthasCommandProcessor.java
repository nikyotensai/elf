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

package com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.commandprocessor.processor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.common.ElfConstants;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RequestData;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.commandprocessor.AbstractCommand;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * @author zhenyu.nie created on 2019 2019/5/22 12:22
 */
@Service
public class ArthasCommandProcessor extends AbstractCommand<String> {

    private static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

    private static final Joiner SPACE_JOINER = Joiner.on(' ');

    private static final String DEFAULT_RELEASE_INFO_PATH = "../webapps/releaseInfo.properties";
    private static final String DEFAULT = "default";
    private static final int QDEBUG_JARDEBUG_LENGTH = 1;
    private static final int QDEBUG_ADD_WITH_CONDITION_LENGTH = 6;
    private static final int QDEBUG_ADD_NO_CONDITION_LENGTH = 4;
    private static final int QDEBUG_REF_LENGTH = 2;
    private static final int QDEBUG_REF_ADDRESS_INDEX = 1;
    private static final int CORE_COMMAND_INDEX = 0;
    private static final int QDEBUG_ADD_SOURCE_INDEX = 2;
    private static final int QDEBUG_ADD_CONDITION_INDEX = 5;
    private static final int QMONITRO_ADD_SOURCE_INDEX = 2;


    @Override
    public Set<Integer> getCodes() {
        return ImmutableSet.of(CommandCode.REQ_TYPE_ARTHAS.getCode(),
                CommandCode.REQ_TYPE_DEBUG.getCode(),
                CommandCode.REQ_TYPE_MONITOR.getCode(),
                CommandCode.REQ_TYPE_JAR_INFO.getCode(),
                CommandCode.REQ_TYPE_CONFIG.getCode(),
                CommandCode.REQ_TYPE_JAR_DEBUG.getCode(),
                CommandCode.REQ_TYPE_PROFILER_INFO.getCode());
    }

    @Override
    protected String prepareCommand(RequestData<String> data, String agentId) {
        String command = encodeCommand(data.getCommand(), data.getApp());
        return command + ElfConstants.PID_PARAM + ElfConstants.FILL_PID;
    }

    @Override
    public int getMinAgentVersion() {
        return -1;
    }

    @Override
    public boolean supportMulti() {
        return false;
    }

    private String encodeCommand(String command, final String appcode) {
        List<String> strs = Lists.newArrayList(SPACE_SPLITTER.splitToList(command));
        if (strs.size() != QDEBUG_ADD_NO_CONDITION_LENGTH && strs.size() != QDEBUG_ADD_WITH_CONDITION_LENGTH && strs.size() != QDEBUG_REF_LENGTH && strs.size() != QDEBUG_JARDEBUG_LENGTH) {
            return command;
        }

        // todo: 感觉可以处理再好一点
        switch (strs.get(CORE_COMMAND_INDEX)) {
            case ElfConstants.REQ_DEBUG_ADD:
                strs.set(QDEBUG_ADD_SOURCE_INDEX, encode(strs.get(QDEBUG_ADD_SOURCE_INDEX)));
                if (strs.size() == QDEBUG_ADD_WITH_CONDITION_LENGTH) {
                    strs.set(QDEBUG_ADD_CONDITION_INDEX, encode(strs.get(QDEBUG_ADD_CONDITION_INDEX)));
                }
                return SPACE_JOINER.join(strs);
            case ElfConstants.REQ_MONITOR_ADD:
                strs.set(QMONITRO_ADD_SOURCE_INDEX, encode(strs.get(QMONITRO_ADD_SOURCE_INDEX)));
                return SPACE_JOINER.join(strs);
            case ElfConstants.REQ_JAR_DEBUG:
            default:
                return command;
        }
    }

    private static String encode(String input) {
        try {
            return URLEncoder.encode(input, "utf8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
