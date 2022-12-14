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

package com.github.nikyotensai.elf.server.proxy.communicate;

import java.util.Set;

import com.github.nikyotensai.elf.remoting.protocol.RequestData;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.UiConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.command.CommunicateCommand;

/**
 * @author zhenyu.nie created on 2019 2019/5/13 14:29
 */
public interface SessionManager {

    Session create(CommunicateCommand command, RequestData requestData, AgentConnection agentConnection, UiConnection uiConnection);

    Session getSession(String id);

    Set<Session> getSessionByUiConnection(UiConnection connection);
}
