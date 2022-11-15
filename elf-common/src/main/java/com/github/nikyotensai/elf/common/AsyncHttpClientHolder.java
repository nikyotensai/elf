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

package com.github.nikyotensai.elf.common;


import java.io.IOException;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

/**
 * @author zhenyu.nie created on 2017 2017/8/25 17:26
 */
public class AsyncHttpClientHolder {

    private static final AsyncHttpClient INSTANCE = initClient();

    private static final int CONN_TIMEOUT = 5000;
    private static final int REQUEST_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 10000;

    public static AsyncHttpClient getInstance() {
        return INSTANCE;
    }

    public static synchronized void close() throws IOException {
        INSTANCE.close();
    }

    private static AsyncHttpClient initClient() {

        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder();
        builder.setConnectTimeout(CONN_TIMEOUT);
        builder.setRequestTimeout(REQUEST_TIMEOUT);
        builder.setReadTimeout(READ_TIMEOUT);
        builder.setCompressionEnforced(false);
        builder.setPooledConnectionIdleTimeout(3 * 60 * 1000);
        builder.setThreadPoolName("async-http-callback");
        builder.setIoThreadsCount(4);
        return new DefaultAsyncHttpClient(builder.build());
    }
}
