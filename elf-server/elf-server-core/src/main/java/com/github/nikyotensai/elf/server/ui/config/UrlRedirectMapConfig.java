package com.github.nikyotensai.elf.server.ui.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "config")
@EnableConfigurationProperties(UrlRedirectMapConfig.class)
public class UrlRedirectMapConfig {

    private Map<String, String> urlRedirect = new HashMap<>();


    public Map<String, String> getUrlRedirect() {
        return urlRedirect;
    }

    public void setUrlRedirect(Map<String, String> urlRedirect) {
        this.urlRedirect = urlRedirect;
    }
}
