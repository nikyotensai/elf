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

package com.github.nikyotensai.elf.server.pojo;

import com.github.nikyotensai.elf.server.store.AppServerStore;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author leix.xie
 * @since 2019/7/2 14:55
 */
@Data
@Accessors(chain = true)
public class AppServer {

    private String serverId;
    private String ip;
    private int port;
    private String host;
    private String logDir;
    private String appCode;
    private boolean autoJStackEnable;
    private boolean autoJMapHistoEnable;
    private boolean connected;


    public AppServer setIp(String ip) {
        this.ip = ip;
        AppServerStore.register(ip, this);
        return this;
    }

}
