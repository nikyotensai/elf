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

import com.github.nikyotensai.elf.attach.arthas.instrument.InstrumentClient;
import com.github.nikyotensai.elf.attach.common.ElfLoggger;
import com.github.nikyotensai.elf.client.common.monitor.MetricsSnapshot;
import com.github.nikyotensai.elf.instrument.client.common.InstrumentInfo;
import com.github.nikyotensai.elf.instrument.client.metrics.Metrics;
import com.github.nikyotensai.elf.instrument.client.metrics.MetricsReportor;
import com.github.nikyotensai.elf.instrument.client.metrics.QMonitorMetricsReportor;
import com.github.nikyotensai.elf.instrument.client.monitor.DefaultMonitor;
import com.github.nikyotensai.elf.instrument.client.monitor.Monitor;
import com.taobao.middleware.logger.Logger;

/**
 * @author leix.xie
 * @since 2018/12/26 20:39
 */
public class QMonitorClient implements InstrumentClient {

    private static final Logger logger = ElfLoggger.getLogger();

    private static final MetricsReportor REPORTOR = new QMonitorMetricsReportor(Metrics.INSTANCE);

    private final Monitor monitor;

    public QMonitorClient(InstrumentInfo instrumentInfo) {
        logger.info("start init qmonitor client");
        try {
            Monitor monitor = new DefaultMonitor();
            monitor.startup(instrumentInfo);

            this.monitor = monitor;
            logger.info("init qmonitor client success");
        } catch (Throwable e) {
            destroy();
            logger.error("", "error init qmonitor client", e);
            throw new IllegalStateException("qmonitor client init error", e);
        }
    }

    public String addMonitor(String source, int line) {
        return monitor.addMonitor(source, line);
    }

    public MetricsSnapshot reportMonitor(final String name) {
        return REPORTOR.report(name);
    }

    public synchronized void destroy() {
        try {
            logger.info("start destroy qmonitorclient");
            Metrics.destroy();
            if (monitor != null) {
                monitor.destroy();
            }
            logger.info("end destroy qmonitorclient");
        } catch (Exception e) {
            logger.error("", "destroy qmonitorclient error", e);
        }
    }
}
