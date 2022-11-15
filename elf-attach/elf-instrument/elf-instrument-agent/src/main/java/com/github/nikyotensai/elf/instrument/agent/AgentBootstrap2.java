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

package com.github.nikyotensai.elf.instrument.agent;

import com.github.nikyotensai.elf.instrument.spy.ElfSpys1;

import java.arthas.Spy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;

/**
 * @author zhenyu.nie created on 2018 2018/11/19 19:39
 */
public class AgentBootstrap2 {

    private static final String ADVICEWEAVER = "com.taobao.arthas.core.advisor.AdviceWeaver";
    private static final String ON_BEFORE = "methodOnBegin";
    private static final String ON_RETURN = "methodOnReturnEnd";
    private static final String ON_THROWS = "methodOnThrowingEnd";
    private static final String BEFORE_INVOKE = "methodOnInvokeBeforeTracing";
    private static final String AFTER_INVOKE = "methodOnInvokeAfterTracing";
    private static final String THROW_INVOKE = "methodOnInvokeThrowTracing";
    private static final String RESET = "resetElfClassLoader";
    private static final String ARTHAS_CONFIGURE = "com.taobao.arthas.core.config.Configure";
    private static final String ELF_BOOTSTRAP = "com.github.nikyotensai.elf.attach.arthas.server.ElfBootstrap";
    private static final String TO_CONFIGURE = "toConfigure";
    private static final String GET_JAVA_PID = "getJavaPid";
    private static final String GET_INSTANCE = "getInstance";
    private static final String IS_BIND = "isBind";
    private static final String BIND = "bind";

    private static final String MAGIC_CLASS_LOADER = "com.github.nikyotensai.elf.magic.loader.MagicClassLoader";
    private static final String MAGIC_CLASSES = "com.github.nikyotensai.elf.magic.classes.MagicClasses";
    private static final String IS_MAGIC_CLASS_METHOD = "isMagicClass";
    private static final String MAGIC_JAR = "elf-magic-classes.jar";

    private static final String ARTHAS_SPY = "arthas-spy.jar";
    private static final String ELF_SPY = "elf-instrument-spy.jar";
    private static final Set<String> SPYS = new HashSet<>(Arrays.asList(ARTHAS_SPY, ELF_SPY));

    private static final String DELIMITER = "\\$\\|\\$";

    private static PrintStream ps = System.err;

    static {
        try {
            File log = new File(System.getProperty("user.home") + File.separator + "logs" + File.separator
                    + "arthas" + File.separator + "arthas.log");
            if (!log.exists()) {
                log.getParentFile().mkdirs();
                log.createNewFile();
            }
            ps = new PrintStream(new FileOutputStream(log, true));
        } catch (Throwable t) {
            t.printStackTrace(ps);
        }
    }

    // 全局持有classloader用于隔离elf实现
    private static volatile ClassLoader elfClassLoader;

    public static void premain(String args, Instrumentation inst) {
        System.getProperties().forEach((k, v) -> System.out.println(k + "=" + v));
        main(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst);
    }

    /**
     * 让下次再次启动时有机会重新加载
     */
    public synchronized static void resetElfClassLoader() {
        elfClassLoader = null;
    }

    private static ClassLoader getClassLoader(Instrumentation inst, List<File> spyJarFiles, File agentJarFile, final String libClass) throws Throwable {
        // 将Spy添加到BootstrapClassLoader
        for (File spyJarFile : spyJarFiles) {
            inst.appendToBootstrapClassLoaderSearch(new JarFile(spyJarFile));
        }

        // 构造自定义的类加载器，尽量减少elf对现有工程的侵蚀
        return loadOrDefineClassLoader(inst, agentJarFile, spyJarFiles, libClass);
    }

    private static ClassLoader loadOrDefineClassLoader(Instrumentation inst, File agentJar, List<File> spyJarFiles, final String libClass) throws Throwable {
        if (elfClassLoader == null) {
            File dir = agentJar.getParentFile();
            File[] jars = getNonSpyJarFiles(dir, spyJarFiles);

            URL[] urls = new URL[jars.length];
            for (int i = 0; i < jars.length; ++i) {
                urls[i] = jars[i].toURI().toURL();
            }
            ps.println("elf classloader urls, " + Arrays.toString(urls));
            elfClassLoader = new ElfClassloader(urls, findUserClassLoader(inst, libClass));
            initMagic((ElfClassloader) elfClassLoader, dir);
        }
        return elfClassLoader;
    }

    private static ClassLoader findUserClassLoader(Instrumentation inst, final String libClass) {
        return findLibClass(inst, libClass).getClassLoader();
    }

    // 这个方法和DefaultDebugger里面是一样的，但是这个地方不应该有依赖，所以两边都要写
    private static Class<?> findLibClass(Instrumentation inst, final String libClass) {
        if (libClass == null || "".equals(libClass)) {
            ps.println("can not find lib class, [" + libClass + "]");
            throw new IllegalStateException("can not find lib class, [" + libClass + "]");
        }

        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        for (Class clazz : allLoadedClasses) {
            if (libClass.equals(clazz.getName())) {
                return clazz;
            }
        }
        ps.println("can not find lib class, [" + libClass + "]");
        ps.println("begin print all loaded classes");
        for (Class allLoadedClass : allLoadedClasses) {
            ps.println("[" + allLoadedClass.getName() + "]");
        }
        ps.println("end print all loaded classes");
        throw new IllegalStateException("can not find lib class, [" + libClass + "]");
    }

    private static File[] getNonSpyJarFiles(File dir, final List<File> spyJarFiles) {
        File[] jars = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (!name.endsWith(".jar")) {
                    return false;
                }

                for (File spyJarFile : spyJarFiles) {
                    if (spyJarFile.getName().equals(name)) {
                        return false;
                    }
                }
                return true;
            }
        });
        if (jars == null) {
            throw new IllegalStateException("no jars");
        }
        return jars;
    }

    private static void initMagic(ElfClassloader elfClassloader, File dir) {
        try {
            File magicJar = getJarFile(dir, MAGIC_JAR);
            URL[] magicJarUrls = {magicJar.toURI().toURL()};

            /**
             * MagicClassLoader magicClassLoader = new MagicClassLoader(magicJarUrls, elfClassLoader);
             */
            Class<?> magicClassLoaderClass = elfClassloader.loadClass(MAGIC_CLASS_LOADER);
            Constructor<?> magicClassLoaderConstructor = magicClassLoaderClass.getDeclaredConstructor(URL[].class, ClassLoader.class);
            ClassLoader magicClassLoader = (ClassLoader) magicClassLoaderConstructor.newInstance(magicJarUrls, elfClassloader);

            elfClassloader.setMagicClassSetting(magicClassLoader);
        } catch (Throwable e) {
            ps.println("init magic error, " + e.getMessage());
            e.printStackTrace(ps);
        }
    }

    private static void initSpy(ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException {
        initArthasSpy(classLoader);
        initQSpy(classLoader);
    }

    private static void initArthasSpy(ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> adviceWeaverClass = classLoader.loadClass(ADVICEWEAVER);
        Method onBefore = adviceWeaverClass.getMethod(ON_BEFORE, int.class, ClassLoader.class, String.class,
                String.class, String.class, Object.class, Object[].class);
        Method onReturn = adviceWeaverClass.getMethod(ON_RETURN, Object.class);
        Method onThrows = adviceWeaverClass.getMethod(ON_THROWS, Throwable.class);
        Method beforeInvoke = adviceWeaverClass.getMethod(BEFORE_INVOKE, int.class, String.class, String.class, String.class, int.class);
        Method afterInvoke = adviceWeaverClass.getMethod(AFTER_INVOKE, int.class, String.class, String.class, String.class, int.class);
        Method throwInvoke = adviceWeaverClass.getMethod(THROW_INVOKE, int.class, String.class, String.class, String.class, int.class);
        Method reset = AgentBootstrap2.class.getMethod(RESET);
        Spy.initForAgentLauncher(classLoader, onBefore, onReturn, onThrows, beforeInvoke, afterInvoke, throwInvoke, reset);
    }

    private static void initQSpy(ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> globalContextClass = classLoader.loadClass("com.github.nikyotensai.elf.instrument.client.debugger.GlobalDebugContext");
        Class<?> snapshotCaptureClass = classLoader.loadClass("com.github.nikyotensai.elf.instrument.client.debugger.SnapshotCapture");
        Class<?> agentMonitorClass = classLoader.loadClass("com.github.nikyotensai.elf.instrument.client.monitor.AgentMonitor");
        ElfSpys1.init(
                globalContextClass.getMethod(ElfSpys1.HAS_BREAKPOINT_SET, String.class, int.class),
                globalContextClass.getMethod(ElfSpys1.IS_HIT, String.class, int.class),
                snapshotCaptureClass.getMethod(ElfSpys1.PUT_LOCAL_VARIABLE, String.class, Object.class),
                snapshotCaptureClass.getMethod(ElfSpys1.PUT_FIELD, String.class, Object.class),
                snapshotCaptureClass.getMethod(ElfSpys1.PUT_STATIC_FIELD, String.class, Object.class),
                snapshotCaptureClass.getMethod(ElfSpys1.FILL_STACKTRACE, String.class, int.class, Throwable.class),
                snapshotCaptureClass.getMethod(ElfSpys1.DUMP, String.class, int.class),
                snapshotCaptureClass.getMethod(ElfSpys1.END_RECEIVE, String.class, int.class),
                agentMonitorClass.getMethod(ElfSpys1.START_MONITOR),
                agentMonitorClass.getMethod(ElfSpys1.STOP_MONITOR, String.class, long.class),
                agentMonitorClass.getMethod(ElfSpys1.EXCEPTION_MONITOR, String.class)
        );
    }

    private static synchronized void main(final String args, final Instrumentation inst) {
        try {
            ps.println("elf server agent start...");

            String[] argsArr = args.split(DELIMITER);
            // 传递的args参数分三个部分:agentJar路径、agentArgs、用户类, 分别是Agent的JAR包路径、期望传递到服务端的参数和用户应用中的类
            String agentJar = argsArr[0];
            final String agentArgs = argsArr[1];
            final String libClass = argsArr[2];

            System.setProperty("elf.app.lib.class", libClass);

            File agentJarFile = new File(agentJar);
            File dir = agentJarFile.getParentFile();
            File realAgentJarFile = getJarFile(dir, agentJarFile.getName());
            List<File> spyJarFiles = new ArrayList<>(SPYS.size());
            for (String spy : SPYS) {
                File spyJarFile = getJarFile(dir, spy);
                spyJarFiles.add(spyJarFile);
            }

            /**
             * Use a dedicated thread to run the binding logic to prevent possible memory leak. #195
             */
            final ClassLoader agentLoader = getClassLoader(inst, spyJarFiles, realAgentJarFile, libClass);
            initSpy(agentLoader);

            Thread bindingThread = new Thread() {
                @Override
                public void run() {
                    try {
                        bind(inst, agentLoader, agentArgs);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace(ps);
                    }
                }
            };

            bindingThread.setName("elf-binding-thread");
            bindingThread.start();
            bindingThread.join();
        } catch (Throwable t) {
            ps.println("loadError:" + t.getMessage());
            t.printStackTrace(ps);
            try {
                if (ps != System.err) {
                    ps.close();
                }
            } catch (Throwable tt) {
                // ignore
            }
            throw new RuntimeException(t);
        }
    }

    private static File getJarFile(File dir, String name) {
        final String prefix = name.substring(0, name.indexOf('.'));
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix);
            }
        });
        if (files == null || files.length != 1) {
            throw new IllegalStateException("illegal jar files, " + Arrays.toString(files));
        }
        return files[0];
    }

    private static void bind(Instrumentation inst, ClassLoader agentLoader, String args) throws Throwable {
        /**
         * <pre>
         * Configure configure = Configure.toConfigure(args);
         * int javaPid = configure.getJavaPid();
         * ElfBootstrap bootstrap = ElfBootstrap.getInstance(javaPid, inst);
         * </pre>
         */
        Class<?> classOfConfigure = agentLoader.loadClass(ARTHAS_CONFIGURE);
        Object configure = classOfConfigure.getMethod(TO_CONFIGURE, String.class).invoke(null, args);
        int javaPid = (Integer) classOfConfigure.getMethod(GET_JAVA_PID).invoke(configure);
        Class<?> bootstrapClass = agentLoader.loadClass(ELF_BOOTSTRAP);
        Object bootstrap = bootstrapClass.getMethod(GET_INSTANCE, int.class, Instrumentation.class).invoke(null, javaPid, inst);
        boolean isBind = (Boolean) bootstrapClass.getMethod(IS_BIND).invoke(bootstrap);
        if (!isBind) {
            try {
                ps.println("elf start to bind...");
                bootstrapClass.getMethod(BIND, classOfConfigure).invoke(bootstrap, configure);
                ps.println("elf server bind success.");
                return;
            } catch (Exception e) {
                ps.println("elf server port binding failed! Please check $HOME/logs/arthas/arthas.log for more details.");
                throw e;
            }
        }
        ps.println("Elf server already bind.");
    }
}
