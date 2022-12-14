package com.github.nikyotensai.elf.server.ui.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.nikyotensai.elf.application.api.AppService;
import com.github.nikyotensai.elf.common.JacksonSerializer;
import com.github.nikyotensai.elf.remoting.command.DownloadCommand;
import com.github.nikyotensai.elf.remoting.protocol.CommandCode;
import com.github.nikyotensai.elf.remoting.protocol.RequestData;
import com.github.nikyotensai.elf.server.common.encryption.DefaultRequestEncryption;
import com.github.nikyotensai.elf.server.common.encryption.RSAEncryption;
import com.github.nikyotensai.elf.server.ui.connection.DownloadWebSocket;
import com.github.nikyotensai.elf.server.ui.security.LoginContext;
import com.github.nikyotensai.elf.server.ui.service.ProxyService;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import cn.hutool.core.util.StrUtil;

/**
 * @author leix.xie
 * @since 2019/11/5 10:42
 */
@Controller
@RequestMapping("/file")
public class DownloadFileController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadFileController.class);

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private AppService appService;

    private DefaultRequestEncryption encryption;

    @PostConstruct
    public void init() {
        try {
            encryption = new DefaultRequestEncryption(new RSAEncryption());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @RequestMapping(value = "download", method = RequestMethod.POST)
    public void download(final String appcode,
                         final String host,
                         final String agentIp,
                         final String path,
                         final String filename,
                         HttpServletResponse response) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(appcode), "app code cannot be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(agentIp), "agent ip cannot be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(path), "path cannot be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(filename), "filename cannot be null or empty");

        String webSocketUrl = proxyService.getWebSocketUrl(agentIp, host);

        if (StrUtil.isEmpty(webSocketUrl)) {
            throw new RuntimeException("not find proxy for agent " + agentIp);
        }

        try {
            OutputStream outputStream = response.getOutputStream();
            URI uri = new URI(webSocketUrl);

            RequestData<String> requestData = buildCommand(appcode, host, path);
            String encrypt = encryption.encrypt(requestData, makeId());

            download(uri, encrypt, filename, outputStream, response);
        } catch (Exception e) {
            logger.error("download fail", e);
        }
    }

    private void download(URI uri, String command, final String filename, final OutputStream outputStream, HttpServletResponse response) throws InterruptedException {
        DownloadWebSocket webSocket = null;
        try {
            CountDownLatch latch = new CountDownLatch(1);
            webSocket = new DownloadWebSocket(uri, latch, outputStream, response, filename, command);
            webSocket.connect();
            latch.await(60, TimeUnit.MINUTES);
        } finally {
            if (webSocket != null) {
                webSocket.close();
            }
        }
    }

    private RequestData<String> buildCommand(final String appCode, final String host, final String path) {
        DownloadCommand downloadCommand = new DownloadCommand();
        downloadCommand.setPath(path);
        String command = JacksonSerializer.serialize(downloadCommand);

        RequestData<String> requestData = new RequestData<>();
        requestData.setApp(appCode);
        requestData.setType(CommandCode.REQ_TYPE_DOWNLOAD_FILE.getOldCode());
        requestData.setHosts(ImmutableList.of(host));
        requestData.setCommand(command);
        requestData.setToken(LoginContext.getLoginContext().getToken());
        requestData.setUser(LoginContext.getLoginContext().getLoginUser());
        return requestData;

    }

    private String makeId() {
        StringBuilder sb = new StringBuilder();
        char[] possible = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        for (int i = 0; i < 8; i++) {
            sb.append(possible[(int) (Math.random() * possible.length)]);
        }
        return sb.toString();
    }

    @ExceptionHandler(Exception.class)
    public void downloadExceptionExHandler(Exception e, HttpServletResponse response) throws IOException {
        logger.error("download fail", e);
        response.setStatus(500);
        response.getOutputStream().write(e.getMessage().getBytes(Charsets.UTF_8));
    }
}
