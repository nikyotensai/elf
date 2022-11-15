package com.github.nikyotensai.elf.application.k8s.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.nikyotensai.elf.server.config.ElfServerProperties;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.ArrayUtil;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.utils.NonBlockingInputStreamPumper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
@Service
@Slf4j
public class K8sService {


    ExecutorService commandExecutorService = Executors.newSingleThreadExecutor();
    private final TimedCache<String, Boolean> cmdExecMap = new TimedCache<>(1000 * 1800, new ConcurrentHashMap<>());

    @Autowired
    private KubernetesClient kubernetesClient;
    @Autowired
    private ElfServerProperties elfServerProperties;


    public DeploymentList deploymentList() {
        return kubernetesClient.apps().deployments()
                .withLabel("java")
                .list();
    }

    public Deployment deployment(String name) {
        return kubernetesClient.apps().deployments()
                .withName(name).get();
    }

    public boolean execOnce(String podName, String command) {
        String cacheKey = getCacheKey(podName, command);
        if (cmdExecMap.containsKey(cacheKey)) {
            return cmdExecMap.get(cacheKey);
        }

        ExecWatch watch = kubernetesClient.pods().withName(podName)
                .redirectingInput()
                .redirectingOutput()
                .redirectingError()
                .redirectingErrorChannel()
                .exec();

        CountDownLatch latch = new CountDownLatch(1);
        try (NonBlockingInputStreamPumper pump = new NonBlockingInputStreamPumper(watch.getOutput(), input -> {
            log.info("command:{},result:{}", command, new String(input));
            latch.countDown();
        })) {
            commandExecutorService.submit(pump);
            watch.getInput().write((command + "\n").getBytes());
            boolean cmdRet = latch.await(5, TimeUnit.SECONDS);
            cmdExecMap.put(cacheKey, cmdRet);
            return cmdRet;
        } catch (Exception ex) {
            log.error("exec command[{}] error", command, ex);
            return false;
        }
    }

    public PodList podList(String name) {
        Deployment deployment = kubernetesClient.apps().deployments()
                .withName(name).get();
        Map<String, String> matchLabels = deployment.getSpec().getSelector().getMatchLabels();
        return kubernetesClient.pods().withLabels(matchLabels).list();
    }


    public FilterWatchListDeletable<Deployment, DeploymentList, Boolean, Watch> labelFilterdDeployment() {
        FilterWatchListDeletable<Deployment, DeploymentList, Boolean, Watch> deployments = kubernetesClient.apps().deployments();
        if (elfServerProperties.getDeploymentFilter() != null
                && elfServerProperties.getDeploymentFilter().getLabelKey() != null) {
            deployments = deployments.withLabel(elfServerProperties.getDeploymentFilter().getLabelKey(),
                    elfServerProperties.getDeploymentFilter().getLabelValue());
        }
        return deployments;
    }


    private String getCacheKey(String... args) {
        return ArrayUtil.join(args, "_");
    }

}
