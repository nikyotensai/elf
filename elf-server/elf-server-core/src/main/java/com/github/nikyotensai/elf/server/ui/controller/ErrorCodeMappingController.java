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

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.nikyotensai.elf.remoting.protocol.ErrorCode;
import com.github.nikyotensai.elf.server.util.ResultHelper;
import com.google.common.collect.Lists;

/**
 * @author leix.xie
 * @since 2019/6/10 11:21
 */
@Controller
public class ErrorCodeMappingController {

    @ResponseBody
    @RequestMapping("api/errorcode/mapping")
    public Object getErrorCodeMapping() {
        ArrayList<ErrorCode> errorCodes = Lists.newArrayList(ErrorCode.values());
        Map<Integer, String> errorCodeMapping = errorCodes.stream().collect(Collectors.toMap(ErrorCode::getCode, ErrorCode::getMessage));
        return ResultHelper.success(errorCodeMapping);
    }
}
