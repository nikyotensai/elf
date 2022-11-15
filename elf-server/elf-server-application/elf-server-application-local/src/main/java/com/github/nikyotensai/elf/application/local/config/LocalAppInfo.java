package com.github.nikyotensai.elf.application.local.config;

import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.github.nikyotensai.elf.remoting.util.LocalHost;
import com.github.nikyotensai.elf.server.pojo.AppServer;
import com.github.nikyotensai.elf.server.pojo.Application;

import lombok.Data;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Component
@ConfigurationProperties(prefix = "elf.local")
@Data
public class LocalAppInfo {

    private static Application application;
    private static AppServer appServer;

    private String appCode = "local";
    private String host = "localhost";
    private String ip = LocalHost.getLocalHost();
    private int port = 80;

    private String logDir = "/opt";


    public Application convert2Application() {
        if (application == null) {
            application = new Application(appCode, appCode, "default", 1, "admin", new Date());
        }
        return application;
    }

    public AppServer convert2AppServer() {
        if (appServer == null) {
            appServer = new AppServer()
                    .setServerId(appCode)
                    .setAppCode(appCode)
                    .setHost(host)
                    .setIp(ip)
                    .setPort(port)
                    .setLogDir(logDir);
        }
        return appServer;
    }

}
