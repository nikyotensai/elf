package com.github.nikyotensai.elf.application.k8s.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.application.api.ApplicationService;
import com.github.nikyotensai.elf.application.k8s.dao.ApplicationDao;
import com.github.nikyotensai.elf.application.k8s.dao.ApplicationUserDao;
import com.github.nikyotensai.elf.server.pojo.Application;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Service
public class K8sApplicationService implements ApplicationService {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private ApplicationUserDao applicationUserDao;

    @Override
    public List<Application> getAllApplications() {
        return applicationDao.getAllApplications();
    }

    @Override
    public List<Application> getAllApplications(String userCode) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userCode), "user code cannot be null or empty");
        List<String> appCodes = this.applicationUserDao.getAppCodesByUserCode(userCode);
        return this.applicationDao.getAllApplications();
    }

    @Override
    public List<String> getAppOwner(String appCode) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appCode), "app code cannot be null or empty");
        return this.applicationUserDao.getUsersByAppCode(appCode);
    }

}
