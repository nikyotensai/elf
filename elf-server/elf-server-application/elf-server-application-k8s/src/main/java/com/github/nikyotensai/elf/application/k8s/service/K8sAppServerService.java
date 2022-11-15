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

package com.github.nikyotensai.elf.application.k8s.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.application.api.AppServerService;
import com.github.nikyotensai.elf.application.k8s.dao.AppServerDao;
import com.github.nikyotensai.elf.server.pojo.AppServer;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Service
public class K8sAppServerService implements AppServerService {


    @Autowired
    private AppServerDao appServerDao;


    @Override
    public List<AppServer> getAppServerByAppCode(final String appCode) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appCode), "app code cannot be null or empty");
        return this.appServerDao.getAppServerByAppCode(appCode);
    }

    @Override
    public AppServer getAppServerByIp(String ip) {
        return this.appServerDao.getAppServerByIp(ip);
    }

}
