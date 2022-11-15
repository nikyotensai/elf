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

package com.github.nikyotensai.elf.server.ui.security;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


/**
 * @author kelly.li
 */
@Accessors(chain = true)
@Getter
@Setter
public class LoginContext {

    private static final LoginContext DEFAULT_CONTEXT = new LoginContext()
            .setLoginUser("admin")
            .setAdmin(true);

    public final static String CONTEXT = "context";

    private String loginUser;
    private boolean isAdmin;
    private String remoteIP;
    private String returnUrl;
    private boolean isAjax;
    private String token;

    public static LoginContext getLoginContext() {
        return DEFAULT_CONTEXT;
    }

}
