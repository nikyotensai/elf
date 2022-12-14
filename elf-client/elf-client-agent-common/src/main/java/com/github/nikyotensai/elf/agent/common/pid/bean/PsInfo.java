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

package com.github.nikyotensai.elf.agent.common.pid.bean;

import java.util.Arrays;

/**
 * @author leix.xie
 * @since 2019/3/13 17:17
 */
public class PsInfo {

    private String user;
    private int pid;
    private String command;
    private String[] params;

    public PsInfo(String user, int pid, String command, String[] params) {
        this.user = user;
        this.pid = pid;
        this.command = command;
        this.params = params;
    }

    public String getUser() {
        return user;
    }

    public int getPid() {
        return pid;
    }

    public String getCommand() {
        return command;
    }

    public String[] getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "PsInfo{" +
                "user='" + user + '\'' +
                ", pid=" + pid +
                ", command='" + command + '\'' +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
