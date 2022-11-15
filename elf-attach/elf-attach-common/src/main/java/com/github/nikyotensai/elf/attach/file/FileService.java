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

package com.github.nikyotensai.elf.attach.file;

import com.github.nikyotensai.elf.attach.file.bean.FileBean;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * @author leix.xie
 * @since 2019-07-25 19:55
 */
public interface FileService {

    String replaceJarWithUnPackDir(String url);

    String readFile(URL url);

    List<FileBean> listFiles(URL url);

    List<FileBean> listFiles(Set<String> exclusionFileSuffix, Set<String> exclusionFile, URL url);
}
