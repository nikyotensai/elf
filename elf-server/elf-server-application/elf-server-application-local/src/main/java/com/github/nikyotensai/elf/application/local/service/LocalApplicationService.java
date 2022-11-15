package com.github.nikyotensai.elf.application.local.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.application.api.ApplicationService;
import com.github.nikyotensai.elf.application.local.config.LocalAppInfo;
import com.github.nikyotensai.elf.server.pojo.Application;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Service
public class LocalApplicationService implements ApplicationService {

    @Autowired
    private LocalAppInfo localAppInfo;

    @Override
    public List<Application> getAllApplications() {
        return Collections.singletonList(localAppInfo.convert2Application());
    }

    @Override
    public List<Application> getAllApplications(String userCode) {
        return getAllApplications();
    }

    @Override
    public List<String> getAppOwner(String appCode) {
        return localAppInfo.convert2Application().getOwner();
    }


}
