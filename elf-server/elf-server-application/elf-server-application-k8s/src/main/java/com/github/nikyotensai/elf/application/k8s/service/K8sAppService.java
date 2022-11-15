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
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.application.api.AppService;
import com.github.nikyotensai.elf.application.k8s.dao.ApplicationDao;
import com.github.nikyotensai.elf.application.k8s.dao.ApplicationUserDao;
import com.github.nikyotensai.elf.server.pojo.Application;
import com.google.common.collect.Sets;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Service
public class K8sAppService implements AppService {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private ApplicationUserDao applicationUserDao;

    @Override
    public Set<String> getApps(String userCode) {
        List<String> appCodes = this.applicationUserDao.getAppCodesByUserCode(userCode);
        return Sets.newHashSet(appCodes);
    }

    @Override
    public Application getAppInfo(String appCode) {
        return this.applicationDao.getApplicationByAppCode(appCode);
    }


}
