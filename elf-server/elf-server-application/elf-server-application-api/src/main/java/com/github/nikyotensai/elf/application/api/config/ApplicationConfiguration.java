package com.github.nikyotensai.elf.application.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Configuration
public class ApplicationConfiguration {


    @ConditionalOnProperty(value = "elf.application.mode", havingValue = "local", matchIfMissing = true)
    @ComponentScan(basePackages = "com.github.nikyotensai.elf.application.local")
    public static class LocalConfig {

    }


    @ConditionalOnProperty(value = "elf.application.mode", havingValue = "k8s", matchIfMissing = false)
    @ComponentScan(basePackages = "com.github.nikyotensai.elf.application.k8s")
    public static class K8sConfig {

    }

}
