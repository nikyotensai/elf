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

package com.github.nikyotensai.elf.server.util;

import com.github.nikyotensai.elf.server.bean.ApiResult;
import com.github.nikyotensai.elf.server.bean.ApiStatus;

/**
 * @author leix.xie
 * @since 2019/7/2 16:02
 */
public class ResultHelper {

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(ApiStatus.SUCCESS.getCode(), "成功", null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ApiStatus.SUCCESS.getCode(), "成功", data);
    }

    public static <T> ApiResult<T> success(int code, String message, T data) {
        return new ApiResult<>(code, message, data);
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(ApiStatus.SUCCESS.getCode(), message, data);
    }

    public static <T> ApiResult<T> fail(String message) {
        return new ApiResult<>(ApiStatus.SYSTEM_ERROR.getCode(), message, null);
    }

    public static <T> ApiResult<T> fail(int status, String message) {
        return new ApiResult<>(status, message, null);
    }

    public static <T> ApiResult<T> fail(int status, String message, T data) {
        return new ApiResult<>(status, message, data);
    }

}
