package com.github.nikyotensai.elf.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nikyotensai
 * @since 2022/9/29
 */
@Slf4j
public class ElfConfigUtil {

    /**
     * 通过环境变量和系统属性覆盖配置
     */
    public static void overrideByConfig() {
        Map<String, String> externalConfigs = new HashMap<>();
        System.getenv().forEach((k, v) -> {
            if (k.startsWith(ElfConstants.PROJECT_NAME)) {
                externalConfigs.put(k, v);
            }
        });
        System.getProperties().forEach((k, v) -> {
            String key = k.toString();
            if (StrUtil.startWithIgnoreCase(key, ElfConstants.PROJECT_NAME.toLowerCase())) {
                externalConfigs.put(key.toUpperCase().replace(".", "_"), v.toString());
            }
        });
        externalConfigs.forEach((k, v) -> {
            Optional.ofNullable(ReflectUtil.getField(ElfClientConfig.class, k)).ifPresent(field -> {
                ReflectUtil.setFieldValue(ElfClientConfig.class, k, v);
                log.info("property [{}]  overrided,new value:{}", k, v);
            });
        });
    }
}
