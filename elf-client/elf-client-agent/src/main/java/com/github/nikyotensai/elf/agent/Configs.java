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

package com.github.nikyotensai.elf.agent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nikyotensai.elf.common.ElfClientConfig;
import com.github.nikyotensai.elf.common.JacksonSerializer;
import com.github.nikyotensai.elf.common.JsonResult;
import com.google.common.base.Strings;

/**
 * @author zhenyu.nie created on 2018 2018/10/25 17:08
 */
class Configs {

    private static final Logger logger = LoggerFactory.getLogger(Configs.class);


    private static final TypeReference<JsonResult<ProxyConfig>> PROXY_REFERENCE = new TypeReference<JsonResult<ProxyConfig>>() {
    };

    private static final String PROXY_URI = "/proxy/config/foragent";


    public static ProxyConfig getProxyConfig() {
        String elfProxyHost = ElfClientConfig.ELF_PROXY_HOST;
        if (Strings.isNullOrEmpty(elfProxyHost)) {
            throw new RuntimeException("system property [elf.proxy.host] cannot be null or empty");
        }
        return getProxyConfig(elfProxyHost);
    }


    private static ProxyConfig getProxyConfig(String elfHost) {
        String url = "http://" + elfHost + PROXY_URI;
        try {
            AsyncHttpClient client = Dsl.asyncHttpClient();
            BoundRequestBuilder builder = client.prepareGet(url);
            builder.setHeader("content-type", "application/json;charset=utf-8");
            Response response = client.executeRequest(builder.build()).get();
            if (response.getStatusCode() != 200) {
                logger.error("get proxy config error, http code [{}], url [{}]", response.getStatusCode(), url);
                return null;
            }

            JsonResult<ProxyConfig> result = JacksonSerializer.deSerialize(response.getResponseBody(StandardCharsets.UTF_8), PROXY_REFERENCE);
            if (!result.isOK()) {
                logger.error("get proxy config error, status code [{}], url [{}]", result.getStatus(), url);
                return null;
            }

            return result.getData();
        } catch (Throwable e) {
            logger.error("get proxy config error, url [{}]", url, e);
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
//        try {
//            StopWatch stopWatch = new StopWatch();
//            stopWatch.start();
//            ProxyConfig xx = getProxyConfig();
//            stopWatch.stop();
//
//            System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        ProxyConfig xx = getProxyConfig();
//        System.out.println(xx);

        long s = System.currentTimeMillis();
        try (AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient()) {
            asyncHttpClient
//                    .prepareGet("http://www.example.com/")
                    .prepareGet("http://" + ElfClientConfig.ELF_PROXY_HOST + PROXY_URI)
                    .execute()
                    .toCompletableFuture()
                    .thenApply(Response::getResponseBody)
                    .thenAccept(System.out::println)
                    .join();
        }
        System.out.println("cost:" + (System.currentTimeMillis() - s));
    }
}
