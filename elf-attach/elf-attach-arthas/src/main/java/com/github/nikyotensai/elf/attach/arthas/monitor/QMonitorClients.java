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

package com.github.nikyotensai.elf.attach.arthas.monitor;

import com.github.nikyotensai.elf.instrument.client.common.InstrumentInfo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhenyu.nie created on 2019 2019/2/18 16:13
 */
public class QMonitorClients {

    private static volatile QMonitorClient client;

    private static AtomicBoolean init = new AtomicBoolean(false);

    public static QMonitorClient getInstance() {
        if (client != null) {
            return client;
        } else {
            throw new IllegalStateException("qdebug client not available");
        }
    }

    public static QMonitorClient create(InstrumentInfo instrumentInfo) {
        if (init.compareAndSet(false, true)) {
            client = new QMonitorClient(instrumentInfo);
            return client;
        } else {
            throw new IllegalStateException("qdebug client already created");
        }
    }
}
