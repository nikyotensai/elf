package com.github.nikyotensai.elf.server.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.nikyotensai.elf.server.pojo.AppServer;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nikyotensai
 * @since 2022/9/30
 */
@Slf4j
public class AppServerStore {

    private static final Map<String, AppServer> appServerMap = new ConcurrentHashMap<>();


    public static void register(String ip, AppServer appServer) {
        appServerMap.put(ip, appServer);
    }

    public static AppServer getServer(String ip) {
        log.info("getServer:{}", ip);
        appServerMap.forEach((k, v) -> {
            log.info("appServer:{}={}", k, v);
        });
        return appServerMap.get(ip);
    }

}
