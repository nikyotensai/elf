package com.github.nikyotensai.elf.server.proxy.util;

import java.util.List;

import com.github.nikyotensai.elf.remoting.protocol.AgentServerInfo;

/**
 * @author leix.xie
 * @since 2019/11/5 15:32
 */
public class DownloadDirUtils {

    private static final String defaultDownloadOtherStr = "/tmp/elf/";
    private static final String defaultDownloadDumpStr = "/tmp/elf/qjtools/qjdump,/tmp/elf-class-dump";


    private static final String ALL_DIR = "all";
    private static final String LOG_DIR = "log";
    private static final String DUMP_DIR = "dump";


    public static String composeDownloadDir(final String appCode, final List<AgentServerInfo> serverInfos, final String type) {
        if (serverInfos == null || serverInfos.isEmpty()) {
            return "";
        }
        String logdir = serverInfos.iterator().next().getLogdir();

        final String appDownloadDump = defaultDownloadDumpStr;
        final String appDonnloadOther = defaultDownloadOtherStr;

        if (ALL_DIR.equalsIgnoreCase(type)) {
            return logdir + "," + appDownloadDump + "," + appDonnloadOther;
        } else if (LOG_DIR.equalsIgnoreCase(type)) {
            return logdir;
        } else if (DUMP_DIR.equalsIgnoreCase(type)) {
            return appDownloadDump;
        } else {
            return appDonnloadOther;
        }
    }
}
