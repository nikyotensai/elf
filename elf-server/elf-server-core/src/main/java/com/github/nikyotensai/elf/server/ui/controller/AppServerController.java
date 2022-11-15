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
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.nikyotensai.elf.application.api.AppServerService;
import com.github.nikyotensai.elf.server.bean.ApiResult;
import com.github.nikyotensai.elf.server.pojo.AppServer;
import com.github.nikyotensai.elf.server.util.ResultHelper;

/**
 * @author leix.xie
 * @since 2019/7/3 16:35
 */
@Controller
@RequestMapping("api/app/server/")
public class AppServerController {

    @Autowired
    private AppServerService appServerService;

    @ResponseBody
    @RequestMapping("list")
    public ApiResult<List<AppServer>> getAppServerByAppCode(final String appCode) {
        return ResultHelper.success(this.appServerService.getAppServerByAppCode(appCode));
    }

    @ResponseBody
    @RequestMapping("autoJStackEnable")
    public ApiResult<Boolean> changeAutoJStackEnable(final String ip, final boolean enable) {
        return ResultHelper.success(this.appServerService.changeAutoJStackEnable(ip, enable));
    }


}
