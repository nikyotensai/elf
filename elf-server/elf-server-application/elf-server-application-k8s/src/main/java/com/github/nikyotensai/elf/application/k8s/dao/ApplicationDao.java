/*
 * Copyright (C) 2019 Qunar, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.nikyotensai.elf.application.k8s.dao;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.nikyotensai.elf.application.k8s.service.K8sService;
import com.github.nikyotensai.elf.server.pojo.Application;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Component
@Slf4j
public class ApplicationDao {

    @Autowired
    K8sService k8sService;

    public Application getApplicationByAppCode(String appCode) {
        return getApplication(deployment(appCode));
    }


    public List<Application> getApplicationsByAppCodes(List<String> appCodes) {
        return appCodes.stream()
                .map(this::deployment)
                .filter(Objects::nonNull)
                .map(this::getApplication)
                .collect(Collectors.toList());
    }


    public List<Application> getAllApplications() {
        DeploymentList deploymentList = k8sService.deploymentList();
        return deploymentList.getItems().stream().map(this::getApplication).collect(Collectors.toList());
    }

    private Deployment deployment(String appCode) {
        return k8sService.deployment(appCode);
    }

    private Application getApplication(Deployment deployment) {
        Application application = new Application();
        String ts = deployment.getMetadata().getCreationTimestamp();
        Date date = new Date(ZonedDateTime.parse(ts).toInstant().toEpochMilli());
        application.setCreateTime(date);
        application.setCreator(deployment.getMetadata().getName());
        application.setGroupCode(deployment.getMetadata().getNamespace());
        application.setName(deployment.getMetadata().getName());
        application.setStatus(1);
        application.setCode(deployment.getMetadata().getName());
        return application;
    }

}
