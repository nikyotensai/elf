package com.github.nikyotensai.elf.application.api;

import java.util.Set;

import com.github.nikyotensai.elf.server.pojo.Application;

/**
 * @author xkrivzooh
 * @since 2019/8/14
 */
public interface AppService {

    Set<String> getApps(String userCode);

    Application getAppInfo(String appCode);

}
