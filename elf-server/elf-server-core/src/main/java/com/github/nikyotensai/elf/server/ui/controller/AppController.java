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

package com.github.nikyotensai.elf.server.ui.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.nikyotensai.elf.application.api.AdminAppService;
import com.github.nikyotensai.elf.application.api.AppServerService;
import com.github.nikyotensai.elf.application.api.AppService;
import com.github.nikyotensai.elf.server.bean.ApiResult;
import com.github.nikyotensai.elf.server.pojo.AppServer;
import com.github.nikyotensai.elf.server.pojo.Application;
import com.github.nikyotensai.elf.server.ui.security.LoginContext;
import com.github.nikyotensai.elf.server.util.ResultHelper;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class AppController {

    private static final int ADMIN_PAGE_SIZE = 20;

    @Autowired
    private AppService appService;

    @Autowired
    private AdminAppService adminAppService;

    @Autowired
    private AppServerService appServerService;

    @RequestMapping("getApps")
    @ResponseBody
    public ApiResult getApps() {
        try {
            final String userName = LoginContext.getLoginContext().getLoginUser();
            return ResultHelper.success(appService.getApps(userName));
        } catch (Exception e) {
            log.error("getApps failed", e);
            return ResultHelper.fail(-1, "获取应用列表失败");
        }
    }

    @RequestMapping("isAdmin")
    @ResponseBody
    public ApiResult<Boolean> isAdmin() {
        return ResultHelper.success(true);
    }

    @RequestMapping("searchApps")
    @ResponseBody
    public ApiResult<List<String>> searchApps(String searchAppKey) {
        return ResultHelper.success(adminAppService.searchApps(searchAppKey, ADMIN_PAGE_SIZE));
    }

    @RequestMapping("getHosts")
    @ResponseBody
    public ApiResult<List<AppServer>> getHosts(@RequestParam(name = "appCode") String appCode) {
        return ResultHelper.success(appServerService.getAppServerByAppCode(appCode));
    }

    @ResponseBody
    @RequestMapping("getAppInfo")
    public ApiResult<Application> getAppInfo(@RequestParam("appCode") String appCode) {
        return ResultHelper.success(appService.getAppInfo(appCode));
    }

}
