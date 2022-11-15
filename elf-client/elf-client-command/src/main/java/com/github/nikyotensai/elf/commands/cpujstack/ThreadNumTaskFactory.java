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

package com.github.nikyotensai.elf.commands.cpujstack;

import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.agent.common.kv.KvDb;
import com.github.nikyotensai.elf.agent.common.kv.KvDbs;
import com.github.nikyotensai.elf.agent.common.util.DateUtils;
import com.github.nikyotensai.elf.remoting.command.ThreadNumCommand;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.github.nikyotensai.elf.remoting.netty.TaskFactory;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RemotingHeader;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * @author zhenyu.nie created on 2019 2019/1/15 11:04
 */
public class ThreadNumTaskFactory implements TaskFactory<ThreadNumCommand> {

    private static final Logger logger = LoggerFactory.getLogger(ThreadNumTaskFactory.class);

    private static final KvDb kvDb = KvDbs.getKvDb();

    private static final int DEFAULT_HOUR_INTERVAL = 2;

    private static final String NAME = "ThreadNum";

    @Override
    public Set<Integer> codes() {
        return ImmutableSet.of(CommandCode.REQ_TYPE_CPU_THREAD_NUM.getCode());
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Task create(RemotingHeader header, ThreadNumCommand command, ResponseHandler handler) {
        DateTime startTime = parseTimeWithoutSecond(command.getStart(), DateTime.now().minusHours(DEFAULT_HOUR_INTERVAL));
        DateTime endTime = parseTimeWithoutSecond(command.getEnd(), DateTime.now());
        return new ThreadNumTask(header.getId(), header.getMaxRunningMs(), kvDb, startTime, endTime, handler);
    }

    private DateTime parseTimeWithoutSecond(String start, DateTime defaultTime) {
        if (!Strings.isNullOrEmpty(start)) {
            return DateUtils.TIME_FORMATTER.parseDateTime(start);
        } else {
            /*
             * 这里先按格式打印出来再去解析出一个DateTime是有原因的。
             * 某些时间可能没有0秒，直接把时间的秒数设置为0好像会报错
             */
            return DateUtils.TIME_FORMATTER.parseDateTime(DateUtils.TIME_FORMATTER.print(defaultTime));
        }
    }
}
