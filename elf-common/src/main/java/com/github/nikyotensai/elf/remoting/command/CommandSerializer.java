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

package com.github.nikyotensai.elf.remoting.command;

import java.util.Objects;

import com.github.nikyotensai.elf.common.JacksonSerializer;
import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;

/**
 * @author leix.xie
 * @since 2019/5/13 17:25
 */
public class CommandSerializer {

    public static <T> T deserializeCommand(String commandLine, Class<T> clazz) {
        if (Objects.equals(clazz, String.class)) {
            return (T) commandLine;
        }
        return JacksonSerializer.deSerialize(commandLine, clazz);
    }

    public static String readCommand(ByteBuf body) {
        int bodyLen = body.readableBytes();
        byte[] bytes = new byte[bodyLen];
        body.readBytes(bytes);
        return new String(bytes, Charsets.UTF_8);
    }
}
