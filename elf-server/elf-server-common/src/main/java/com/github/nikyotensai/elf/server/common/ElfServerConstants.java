package com.github.nikyotensai.elf.server.common;

import java.io.File;

/**
 * @author cai.wen created on 19-12-27 上午10:59
 */
public class ElfServerConstants {

    public static final String PROFILER_ROOT_PATH = System.getProperty("java.io.tmpdir") + File.separator + "elf-profiler";

    public static final String PROFILER_ROOT_TEMP_PATH = PROFILER_ROOT_PATH + File.separator + "tmp";

    public static final String PROFILER_ROOT_AGENT_PATH = PROFILER_ROOT_PATH + File.separator + "agent";
}
