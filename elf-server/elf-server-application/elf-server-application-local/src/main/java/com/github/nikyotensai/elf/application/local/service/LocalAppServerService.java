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

package com.github.nikyotensai.elf.application.local.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.application.api.AppServerService;
import com.github.nikyotensai.elf.application.local.config.LocalAppInfo;
import com.github.nikyotensai.elf.server.pojo.AppServer;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Service
public class LocalAppServerService implements AppServerService {


    @Autowired
    private LocalAppInfo localAppInfo;


    @Override
    public List<AppServer> getAppServerByAppCode(final String appCode) {
        return Collections.singletonList(localAppInfo.convert2AppServer());
    }

    @Override
    public AppServer getAppServerByIp(String ip) {
        return localAppInfo.convert2AppServer();
    }

}
