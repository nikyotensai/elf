package com.github.nikyotensai.elf.common;

import com.github.nikyotensai.elf.remoting.util.LocalHost;

/**
 * @author nikyotensai
 * @since 2022/9/28
 */
public class ElfClientConfig {

    /**
     * ELF主路径
     */
    public static String ELF_AGENT_PATH = "/elf/agent";
    /**
     * elf agent数据存放路径，包括sqlite存放的监控、jstack及jmap数据和反编译代码临时文件的存放
     */
    public static String ELF_STORE_PATH = "/tmp/elf/store";
    /**
     * server的host
     */
    public static String ELF_PROXY_HOST = LocalHost.getLocalHost() + ":9091";
    /**
     * 应用依赖的jar包中的一个类
     */
    public static String ELF_APP_LIB_CLASS = "org.springframework.web.servlet.DispatcherServlet";
    public static String ELF_PID_HANDLER_JPS_SYMBOL_CLASS = "org.apache.catalina.startup.Bootstrap";

    public static String ELF_APP_CLASSES_PATH = "";
    public static String ELF_AGENT_WORKGROUP_NUM = "2";
    public static String ELF_AGENT_THREAD_NUM = "16";


    public static String getLibDir() {
        return ELF_AGENT_PATH + "/lib";
    }


}
