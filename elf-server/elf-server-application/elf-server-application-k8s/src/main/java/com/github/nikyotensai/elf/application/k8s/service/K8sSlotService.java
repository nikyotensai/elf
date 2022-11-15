package com.github.nikyotensai.elf.application.k8s.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.application.api.SlotService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Service
@Slf4j
public class K8sSlotService implements SlotService {

    private static final String START_AGENT_COMMAND = "nohup sh /elf/agent/bin/start-elf-agent.sh &";

    @Autowired
    private K8sService k8sService;

    @Override
    public void beforeGetSocket(String host) {
        // 启动agent
        boolean loaded = k8sService.execOnce(host, START_AGENT_COMMAND);
        log.info("host:{},execute command:{}", host, START_AGENT_COMMAND);
        if (loaded) {
            log.info("loadAgent success");
        }
    }
}
