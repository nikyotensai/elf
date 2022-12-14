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

package com.github.nikyotensai.elf.attach.arthas.jar;

import com.github.nikyotensai.elf.attach.arthas.instrument.InstrumentClient;
import com.github.nikyotensai.elf.attach.common.ElfLoggger;
import com.github.nikyotensai.elf.attach.file.FileOperateFactory;
import com.github.nikyotensai.elf.attach.file.bean.FileBean;
import com.github.nikyotensai.elf.instrument.client.common.InstrumentInfo;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.taobao.middleware.logger.Logger;

import java.io.File;
import java.util.List;

/**
 * @author leix.xie
 * @since 2019/2/12 16:59
 */
public class JarInfoClient implements InstrumentClient {

    private static final Logger logger = ElfLoggger.getLogger();

    private final List<String> jarInfos;

    JarInfoClient(InstrumentInfo instrumentInfo) {
        logger.info("start init jar info client");
        List<String> jarInfoList = ImmutableList.of();
        try {
            jarInfoList = listJar(instrumentInfo);
            logger.info("init jar info client success");
        } catch (Throwable e) {
            destroy();
            logger.error("", "jar info client init error", e);
        }
        jarInfos = jarInfoList;
    }

    public List<String> jarInfo() {
        return jarInfos;
    }

    private List<String> listJar(InstrumentInfo instrumentation) {
        return readJarInfos(instrumentation.getSystemClass());
    }

    private static List<String> readJarInfos(Class clazz) {
        String serverManagerJarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        File jarDir = new File(serverManagerJarPath).getParentFile();
        List<FileBean> fileBeans = FileOperateFactory.listFiles(jarDir.getPath());
        logger.info("agent ?????? jar ??????");
        List<String> result = Lists.transform(fileBeans, new Function<FileBean, String>() {
            @Override
            public String apply(FileBean file) {
                String name = file.getName();
                return name.substring(name.lastIndexOf(File.separatorChar) + 1);
            }
        });
        return result;
    }

    @Override
    public void destroy() {

    }
}
