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

package com.github.nikyotensai.elf.remoting.netty;

import java.util.Set;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.remoting.protocol.RemotingHeader;

/**
 * @author zhenyu.nie created on 2019 2019/5/28 15:38
 */
public interface TaskFactory<T> {

    Set<Integer> codes();

    String name();

    Task create(RemotingHeader header, T command, ResponseHandler handler);
}
