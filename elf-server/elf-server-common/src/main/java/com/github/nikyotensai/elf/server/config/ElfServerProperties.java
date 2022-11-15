package com.github.nikyotensai.elf.server.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.github.nikyotensai.elf.remoting.util.LocalHost;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "elf")
@Data
public class ElfServerProperties {


    private Map<String, String> location;

    private ServerConf server;

    private LabelFilter deploymentFilter;

    @Data
    public static class ServerConf {

        private String host = LocalHost.getLocalHost();

        private int port4Agent = 9014;
        private int heartbeatSec = 30;

        private int port4Ui = 9013;

        private String webSocketUrl = "ws://" + host + ":" + port4Ui + "/ws";
    }

    @Data
    public static class LabelFilter {

        private String labelKey;
        private String labelValue;
    }

}
