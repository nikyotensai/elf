package com.github.nikyotensai.elf.application.local.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.application.api.AdminAppService;
import com.github.nikyotensai.elf.application.local.config.LocalAppInfo;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Service
public class LocalAdminAppService implements AdminAppService {


    @Autowired
    private LocalAppInfo localAppInfo;

    @Override
    public List<String> searchApps(String keyInput, int size) {
        return Collections.singletonList(localAppInfo.getAppCode());
    }

}
