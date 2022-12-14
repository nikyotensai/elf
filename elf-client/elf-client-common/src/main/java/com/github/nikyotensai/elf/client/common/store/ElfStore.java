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

package com.github.nikyotensai.elf.client.common.store;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import com.github.nikyotensai.elf.common.ElfClientConfig;
import com.github.nikyotensai.elf.common.FileUtil;

/**
 * @author leix.xie
 * @since 2019-07-22 15:46
 */
public class ElfStore {

    private static final String DEFAULT_PATH = "/tmp/elf/store";

    private static final String DEFAULT_CHILD = "default";
    private static final String STORE_PATH;

    private static final AtomicReference<String> DEFAULT_PROFILER_STORE_PATH;

    private static final String DEFAULT_PROFILER_ROOT_DIR = "elf-profiler";

    private static final String DEFAULT_PROFILER_TEMP_DIR = "elf-profiler-tmp";

    static {
        new File(DEFAULT_PATH).mkdirs();
        String path = ElfClientConfig.ELF_STORE_PATH;

        if (path == null) {
            path = System.getProperty("catalina.base");
            if (path == null) {
                path = System.getProperty("java.io.tmpdir");
            }
            path = path + File.separator + "cache";
            ElfClientConfig.ELF_STORE_PATH = path;
        }
        STORE_PATH = ElfClientConfig.ELF_STORE_PATH;
        DEFAULT_PROFILER_STORE_PATH = new AtomicReference<>(path);
        FileUtil.ensureDirectoryExists(STORE_PATH);
    }

    public static String getStorePath(final String child) {
        return FileUtil.dealPath(STORE_PATH, child);
    }

    public static String getRootStorePath() {
        return STORE_PATH;
    }

    public static String getDumpFileStorePath() {
        return getStorePath("dump");
    }

    public static String getDefaultStorePath() {
        return getStorePath(DEFAULT_CHILD);
    }

    public static void changeProfilerStorePath(String profilerStorePath) {
        //todo ?????????????????????
        DEFAULT_PROFILER_STORE_PATH.compareAndSet(STORE_PATH, profilerStorePath);
    }

    public static final String DEFAULT_PROFILER_ROOT_PATH = FileUtil.dealPath(DEFAULT_PROFILER_STORE_PATH.get(), DEFAULT_PROFILER_ROOT_DIR);

    public static final String DEFAULT_PROFILER_TEMP_PATH = FileUtil.dealPath(DEFAULT_PROFILER_STORE_PATH.get(), DEFAULT_PROFILER_TEMP_DIR);

    public static String getProfilerStorePath() {
        return DEFAULT_PROFILER_STORE_PATH.get();
    }

    public static String getProfilerRootPath() {
        return FileUtil.dealPath(DEFAULT_PROFILER_STORE_PATH.get(), DEFAULT_PROFILER_ROOT_DIR);
    }

    public static String getProfilerTempPath() {
        return FileUtil.dealPath(DEFAULT_PROFILER_STORE_PATH.get(), DEFAULT_PROFILER_TEMP_DIR);
    }

}
