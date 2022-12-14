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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingBuilder;
import com.github.nikyotensai.elf.remoting.protocol.RequestData;
import com.github.nikyotensai.elf.remoting.protocol.payloadHolderImpl.RequestPayloadHolder;
import com.github.nikyotensai.elf.server.proxy.communicate.agent.AgentConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.UiConnection;
import com.github.nikyotensai.elf.server.proxy.communicate.ui.command.CommunicateCommand;
import com.github.nikyotensai.elf.server.proxy.generator.IdGenerator;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * @author zhenyu.nie created on 2019 2019/5/13 14:53
 */
@Service
public class DefaultSessionManager implements SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSessionManager.class);

    @Autowired
    private IdGenerator sessionIdGenerator;

    private final ConcurrentMap<String, Session> sessions = Maps.newConcurrentMap();

    private final ConcurrentMap<Connection, Set<Session>> uiConnectionToSessionsMapping = Maps.newConcurrentMap();

    private final ConcurrentMap<Connection, Set<Connection>> agentConnectionToUiConnectionMapping = Maps.newConcurrentMap();

    @Override
    public Session create(CommunicateCommand command, RequestData requestData, AgentConnection agentConnection, UiConnection uiConnection) {
        String id = sessionIdGenerator.generateId();
        Session session = new DefaultSession(id, command.isSupportPause(), requestData, agentConnection, uiConnection);
        Session oldSession = sessions.putIfAbsent(id, session);
        if (oldSession != null) {
            return oldSession;
        }

        session.getEndState().addListener(() -> {
            sessions.remove(id);
            Set<Session> uiSessions = uiConnectionToSessionsMapping.getOrDefault(uiConnection, Collections.emptySet());
            uiSessions.remove(session);
        }, MoreExecutors.directExecutor());

        uiConnection.closeFuture().addListener(() -> agentConnectionToUiConnectionMapping
                        .getOrDefault(agentConnection, Collections.emptySet())
                        .remove(uiConnection),
                MoreExecutors.directExecutor());

        doWithConnectionClose(uiConnection, uiConnectionToSessionsMapping, session, theSession -> {
            theSession.writeToAgent(RemotingBuilder.buildRequestDatagram(
                    CommandCode.REQ_TYPE_CANCEL.getCode(), theSession.getId(), new RequestPayloadHolder(theSession.getId())));
            theSession.broken();
        });
        doWithConnectionClose(agentConnection, agentConnectionToUiConnectionMapping, uiConnection, Connection::close);
        return session;
    }

    @Override
    public Session getSession(String id) {
        return sessions.get(id);
    }

    @Override
    public Set<Session> getSessionByUiConnection(UiConnection connection) {
        return uiConnectionToSessionsMapping.getOrDefault(connection, Collections.emptySet());
    }

    private <T> void doWithConnectionClose(Connection connection,
                                           ConcurrentMap<Connection, Set<T>> connectionMapping,
                                           T item,
                                           Consumer<T> consumer) {
        AddTo addTo = addItemToRelativeSet(connectionMapping, connection, item);
        if (addTo == AddTo.newSet) {
            connection.closeFuture().addListener(() -> onConnectionClose(connectionMapping, connection, consumer), MoreExecutors.directExecutor());
        }

        if (connection.closeFuture().isDone()) {
            consumer.accept(item);
        }
    }

    private <T> AddTo addItemToRelativeSet(ConcurrentMap<Connection, Set<T>> connectionMapping, Connection connection, T item) {
        Set<T> items = connectionMapping.get(connection);
        AddTo addTo = AddTo.oldSet;
        if (items == null) {
            Set<T> newSet = createRelativeSet();
            Set<T> oldSet = connectionMapping.putIfAbsent(connection, newSet);
            if (oldSet == null) {
                items = newSet;
                addTo = AddTo.newSet;
            } else {
                items = oldSet;
            }
        }
        items.add(item);
        return addTo;
    }

    private enum AddTo {
        newSet, oldSet
    }

    private <T> void onConnectionClose(ConcurrentMap<Connection, Set<T>> connectionMapping, Connection connection, Consumer<T> consumer) {
        Set<T> items = connectionMapping.remove(connection);
        if (items != null) {
            for (T item : items) {
                consumer.accept(item);
            }
        }
    }

    private <T> Set<T> createRelativeSet() {
        ConcurrentMap<T, Boolean> map = new MapMaker().weakKeys().makeMap();
        return Sets.newSetFromMap(map);
    }
}
