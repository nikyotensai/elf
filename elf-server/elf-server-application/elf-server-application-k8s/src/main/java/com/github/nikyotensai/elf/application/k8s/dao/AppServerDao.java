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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.nikyotensai.elf.application.k8s.service.K8sService;
import com.github.nikyotensai.elf.server.pojo.AppServer;
import com.github.nikyotensai.elf.server.store.AppServerStore;

import io.fabric8.kubernetes.api.model.PodList;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Component
public class AppServerDao {


    @Autowired
    K8sService k8sService;


    public List<AppServer> getAppServerByAppCode(String appCode) {
        PodList podList = k8sService.podList(appCode);
        return podList.getItems().stream().map(pod -> {
            AppServer server = new AppServer();
            server.setAppCode(appCode);
            server.setHost(pod.getMetadata().getName());
            server.setIp(pod.getStatus().getPodIP());
            server.setLogDir("/logs");
            server.setPort(80);
            server.setAutoJStackEnable(true);
            AppServerStore.register(server.getIp(), server);
            return server;
        }).collect(Collectors.toList());

    }


    public AppServer getAppServerByIp(String ip) {
        return AppServerStore.getServer(ip);
    }


}
