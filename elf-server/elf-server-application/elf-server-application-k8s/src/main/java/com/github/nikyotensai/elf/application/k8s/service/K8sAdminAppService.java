package com.github.nikyotensai.elf.application.k8s.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.application.api.AdminAppService;
import com.github.nikyotensai.elf.application.api.ApplicationService;
import com.github.nikyotensai.elf.common.NamedThreadFactory;
import com.github.nikyotensai.elf.server.pojo.Application;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Service
public class K8sAdminAppService implements AdminAppService {

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
            Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("refresh-all-app"));

    private volatile ImmutableSet<String> adminApps = ImmutableSet.of();

    @Autowired
    private ApplicationService applicationService;

    @PostConstruct
    public void init() {
        refreshAllApp();

        SCHEDULED_EXECUTOR_SERVICE.scheduleWithFixedDelay(this::refreshAllApp, 10, 10, TimeUnit.MINUTES);
    }

    private void refreshAllApp() {
        List<Application> allApplications = applicationService.getAllApplications();
        adminApps = ImmutableSet.copyOf(Lists.transform(allApplications, Application::getCode));
    }

    @Override
    public List<String> searchApps(String keyInput, int size) {
        final String key = Strings.nullToEmpty(keyInput).toLowerCase();
        ImmutableSet<String> adminApps = this.adminApps;
        if (Strings.isNullOrEmpty(key)) {
            if (adminApps.size() <= size) {
                return adminApps.asList();
            }
            return adminApps.asList().subList(0, size);
        }

        int needAddSize = size;
        List<String> matchApps = new ArrayList<>(size);
        if (adminApps.contains(key)) {
            matchApps.add(key);
            needAddSize--;
        }

        adminApps.stream()
                .filter((app) -> app.contains(key) && !app.equals(key))
                .limit(needAddSize)
                .forEach(matchApps::add);
        return matchApps;
    }
}
