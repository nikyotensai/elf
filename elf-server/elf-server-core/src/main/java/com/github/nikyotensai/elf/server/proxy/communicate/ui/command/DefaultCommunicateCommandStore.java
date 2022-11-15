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

package com.github.nikyotensai.elf.server.proxy.communicate.ui.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.server.proxy.communicate.ui.handler.commandprocessor.CommunicateCommandProcessor;

/**
 * @author zhenyu.nie created on 2019 2019/5/22 12:13
 */
@Service
public class DefaultCommunicateCommandStore implements CommunicateCommandStore {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCommunicateCommandStore.class);
    private Map<Integer, CommunicateCommand> commandMap = new HashMap<>();

    @Autowired
    public DefaultCommunicateCommandStore(List<UiRequestCommand> commands) {
        for (UiRequestCommand command : commands) {
            registerCommandProcessor(command);
        }
    }

    private void registerCommandProcessor(UiRequestCommand command) {
        Set<Integer> codes = command.getCodes();
        int minAgentVersion = command.getMinAgentVersion();
        boolean supportMulti = command.supportMulti();
        boolean supportPause = command.supportPause();
        CommunicateCommandProcessor processor = command.getProcessor();
        for (Integer code : codes) {
            commandMap.put(code, new CommunicateCommand(code, minAgentVersion, supportMulti, supportPause, processor));
            logger.info("register communicate command processor, code: {}, minAgentVersion: {}, supportMulti: {}, processor: {}", code, minAgentVersion, supportMulti, processor.getClass().getName());
        }
    }

    @Override
    public Optional<CommunicateCommand> getCommunicateCommand(int code) {
        CommunicateCommand command = commandMap.get(code);
        if (command != null) {
            return Optional.of(command);
        }
        return Optional.empty();
    }

}
