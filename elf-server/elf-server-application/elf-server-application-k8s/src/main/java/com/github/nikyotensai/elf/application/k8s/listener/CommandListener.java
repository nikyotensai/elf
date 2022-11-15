package com.github.nikyotensai.elf.application.k8s.listener;

import io.fabric8.kubernetes.client.dsl.ExecListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

/**
 * @author nikyotensai
 * @since 2022/10/14
 */
@AllArgsConstructor
@Slf4j
public class CommandListener implements ExecListener {

    private String podName;

    private String command;


    @Override
    public void onOpen(Response response) {

    }

    @Override
    public void onFailure(Throwable t, Response response) {

    }

    @Override
    public void onClose(int code, String reason) {

    }
}
