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

package com.github.nikyotensai.elf.attach.arthas.server;

import com.github.nikyotensai.elf.attach.arthas.instrument.InstrumentClientStore;
import com.github.nikyotensai.elf.common.ElfConstants;
import com.taobao.arthas.core.config.Configure;
import com.taobao.arthas.core.shell.ShellServer;
import com.taobao.arthas.core.shell.ShellServerOptions;
import com.taobao.arthas.core.shell.command.CommandResolver;
import com.taobao.arthas.core.shell.handlers.BindHandler;
import com.taobao.arthas.core.shell.term.impl.TelnetTermServer;
import com.taobao.arthas.core.util.Constants;
import com.taobao.arthas.core.util.LogUtil;
import com.taobao.arthas.core.util.UserStatUtil;
import com.taobao.middleware.logger.Logger;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhenyu.nie created on 2018 2018/11/19 19:49
 */
public class ElfBootstrap {

    private static Logger logger = LogUtil.getArthasLogger();
    private static ElfBootstrap elfBootstrap;

    private AtomicBoolean isBindRef = new AtomicBoolean(false);
    private int pid;
    private Instrumentation instrumentation;
    private Thread shutdown;
    private ShellServer shellServer;
    private ExecutorService executorService;

    private ElfBootstrap(int pid, Instrumentation instrumentation) {
        this.pid = pid;
        this.instrumentation = instrumentation;

        executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread t = new Thread(r, ElfConstants.ELF_COMMAND_THREAD_NAME);
                t.setDaemon(true);
                return t;
            }
        });

        shutdown = new Thread("elf-shutdown-hooker") {

            @Override
            public void run() {
                ElfBootstrap.this.destroy();
            }
        };

        Runtime.getRuntime().addShutdownHook(shutdown);
    }

    /**
     * Bootstrap elf server
     *
     * @param configure ????????????
     * @throws IOException ?????????????????????
     */
    public void bind(Configure configure) throws Throwable {

        long start = System.currentTimeMillis();

        if (!isBindRef.compareAndSet(false, true)) {
            throw new IllegalStateException("already bind");
        }

        try {
            InstrumentClientStore.init(instrumentation);

            ShellServerOptions options = new ShellServerOptions()
                    .setInstrumentation(instrumentation)
                    .setPid(pid)
                    .setWelcomeMessage(ElfConstants.ELF_VERSION_LINE_PREFIX + ElfConstants.CURRENT_VERSION);
            shellServer = new ShellServerImpl(options, this);
            QBuiltinCommandPack builtinCommands = new QBuiltinCommandPack();
            List<CommandResolver> resolvers = new ArrayList<CommandResolver>();
            resolvers.add(builtinCommands);
            // TODO: discover user provided command resolver
            shellServer.registerTermServer(new TelnetTermServer(
                    configure.getIp(), configure.getTelnetPort(), options.getConnectionTimeout()));

            for (CommandResolver resolver : resolvers) {
                shellServer.registerCommandResolver(resolver);
            }

            shellServer.listen(new BindHandler(isBindRef));

            logger.info("elf-server listening on network={};telnet={};timeout={};", (Object) configure.getIp(),
                    configure.getTelnetPort(), options.getConnectionTimeout());

            logger.info("elf-server started in {} ms", System.currentTimeMillis() - start);
        } catch (Throwable e) {
            logger.error(null, "Error during bind to port " + configure.getTelnetPort(), e);
            if (shellServer != null) {
                shellServer.close();
            }

            InstrumentClientStore.destroy();

            isBindRef.compareAndSet(true, false);
            throw e;
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @return true:?????????????????????;false:???????????????
     */
    public boolean isBind() {
        return isBindRef.get();
    }

    public void destroy() {
        executorService.shutdownNow();
        UserStatUtil.destroy();
        // clear the reference in Spy class.
        cleanUpSpyReference();
        try {
            Runtime.getRuntime().removeShutdownHook(shutdown);
        } catch (Throwable t) {
            // ignore
        }
        logger.info("elf-server destroy completed.");
        // see middleware-container/arthas/issues/123
        try {
            LogUtil.closeResultLogger();
        } catch (Throwable e) {
            logger.error("qlogger-001", "close logger error", e);
        }

        try {
            // ?????????????????????log4j??????
            closeResultLog4jLogger();
        } catch (Throwable e) {
            logger.error("qlogger-002", "close log4j logger error", e);
        }
    }

    private void closeResultLog4jLogger() throws Exception {
        String name = LogUtil.getResultLogger().getName();
        Class<?> logManagerClass = Class.forName("org.apache.log4j.LogManager");
        Method getLogger = logManagerClass.getDeclaredMethod("getLogger", String.class);
        Object resultLog4jLogger = getLogger.invoke(null, name);
        if (resultLog4jLogger != null) {
            // ??????????????????????????????????????????????????????????????????interrupt??????
            Class<?> logHelper = Class.forName("org.apache.log4j.helpers.LogLog");
            Method setQuietMode = logHelper.getMethod("setQuietMode", boolean.class);
            setQuietMode.invoke(null, true);

            Method removeAllAppenders = resultLog4jLogger.getClass().getMethod("removeAllAppenders");
            removeAllAppenders.invoke(resultLog4jLogger);
        }
    }

    /**
     * ??????
     *
     * @param instrumentation JVM??????
     * @return ElfServer??????
     */
    public synchronized static ElfBootstrap getInstance(int javaPid, Instrumentation instrumentation) {
        if (elfBootstrap == null) {
            elfBootstrap = new ElfBootstrap(javaPid, instrumentation);
        }
        return elfBootstrap;
    }

    /**
     * @return ElfServer??????
     */
    public static ElfBootstrap getInstance() {
        if (elfBootstrap == null) {
            throw new IllegalStateException("ElfBootstrap must be initialized before!");
        }
        return elfBootstrap;
    }

    public void execute(Runnable command) {
        executorService.execute(command);
    }

    /**
     * ??????spy??????classloader??????????????????????????????
     */
    private void cleanUpSpyReference() {
        try {
            spyDestroy(Constants.SPY_CLASSNAME);
        } catch (ClassNotFoundException e) {
            logger.error(null, "arthas spy load failed from ElfClassLoader, which should not happen", e);
        } catch (Exception e) {
            logger.error(null, "arthas spy destroy failed: ", e);
        }

        try {
            spyDestroy(ElfConstants.SPY_CLASSNAME);
        } catch (ClassNotFoundException e) {
            logger.error(null, "elf spy load failed from ElfClassLoader, which should not happen", e);
        } catch (Exception e) {
            logger.error(null, "elf spy destroy failed: ", e);
        }
    }

    private void spyDestroy(String spyClassname) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> spyClass = this.getClass().getClassLoader().loadClass(spyClassname);
        Method agentDestroyMethod = spyClass.getMethod("destroy");
        agentDestroyMethod.invoke(null);
    }
}
