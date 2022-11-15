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

import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.common.ElfConstants;
import com.github.nikyotensai.elf.remoting.command.MachineCommand;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RequestData;
import com.github.nikyotensai.elf.server.config.ElfServerProperties;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnectionStore;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.commandprocessor.AbstractCommand;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author leix.xie
 * @since 2019/5/22 16:25
 */
@Service
public class JavaCommandProcessor extends AbstractCommand<MachineCommand> {

    private static final String LOCATION = ".location";
    private static final String JSTACK = "jstack";
    private static final String JSTAT = "jstat";

    private static final Set<String> JAVA_COMMAND = ImmutableSet.of(JSTACK, JSTAT);
    private static final int DUMP_DIR_MIN_VERSION = 12;

    @Autowired
    ElfServerProperties elfProxyProperties;

    @PostConstruct
    public void init() {

    }

    @Autowired
    private AgentConnectionStore agentConnectionStore;


    @Override
    protected Optional<RequestData<MachineCommand>> doPreprocessor(RequestData<MachineCommand> requestData, ChannelHandlerContext ctx) {
        String command = requestData.getCommand().getCommand();
        final String commandLocation = elfProxyProperties.getLocation().get(command.trim());
        if (Strings.isNullOrEmpty(commandLocation) || !JAVA_COMMAND.contains(command)) {
            return Optional.empty();
        }
        return Optional.of(requestData);
    }

    protected MachineCommand prepareCommand(RequestData<MachineCommand> requestData, String agentId) {
        String command = requestData.getCommand().getCommand();
        String newCommand = command;
        final String commandLocation = elfProxyProperties.getLocation().get(command.trim());

        if (JSTACK.equals(command)) {
            newCommand = getJstackCommand(agentId, commandLocation);
        } else if (JSTAT.equals(command)) {
            newCommand = commandLocation + " -gcutil " + ElfConstants.FILL_PID + " 1000 1000";
        }

        MachineCommand machineCommand = new MachineCommand();
        machineCommand.setCommand(newCommand);
        machineCommand.setWorkDir(requestData.getAgentServerInfos().iterator().next().getLogdir());
        return machineCommand;
    }

    private String getJstackCommand(String agentId, final String commandLocation) {
        Optional<AgentConnection> optional = agentConnectionStore.getConnection(agentId);
        if (optional.isPresent()) {
            AgentConnection connection = optional.get();
            int version = connection.getVersion();
            if (version >= DUMP_DIR_MIN_VERSION) {
                return commandLocation + " " + ElfConstants.FILL_PID + " " + ElfConstants.FILL_DUMP_TARGET;
            }
        }
        return commandLocation + " " + ElfConstants.FILL_PID;
    }

    @Override
    public Set<Integer> getCodes() {
        return ImmutableSet.of(CommandCode.REQ_TYPE_JAVA.getCode());
    }

    @Override
    public int getMinAgentVersion() {
        return -1;
    }

    @Override
    public boolean supportMulti() {
        return true;
    }
}
