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

package com.github.nikyotensai.elf.instrument.client.common;

import java.util.concurrent.locks.Lock;

/**
 * @author zhenyu.nie created on 2019 2019/2/19 15:46
 */
public interface ClassFileBuffer {

    byte[] getClassBuffer(Class clazz, byte[] defaultBuffer);

    void setClassBuffer(Class clazz, byte[] buffer);

    Lock getLock();

    void destroy();
}
