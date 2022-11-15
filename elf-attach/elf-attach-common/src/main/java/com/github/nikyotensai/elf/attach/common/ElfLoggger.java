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

package com.github.nikyotensai.elf.attach.common;

import com.taobao.middleware.logger.Level;
import com.taobao.middleware.logger.Logger;
import com.taobao.middleware.logger.LoggerFactory;
import com.taobao.middleware.logger.support.LogLog;

/**
 * @author leix.xie
 * @since 2019/1/17 16:17
 */
public class ElfLoggger {
    private static final Logger jsonLogger;

    private static final Logger logger;

    static {
        LogLog.setQuietMode(true);

        int maxBackupIndex = Integer.valueOf(System.getProperty("elf.log.max.backup.index", "7"));

        jsonLogger = LoggerFactory.getLogger("elf-json");
        jsonLogger.activateAppenderWithTimeAndSizeRolling("elf-json", "elf-json.log", "UTF-8", "100MB", "yyyy-MM-dd", maxBackupIndex);
        jsonLogger.setLevel(Level.INFO);
        jsonLogger.setAdditivity(false);

        logger = LoggerFactory.getLogger("elf");
        logger.activateAppenderWithTimeAndSizeRolling("elf", "elf.log", "UTF-8", "100MB", "yyyy-MM-dd", maxBackupIndex);
        logger.setLevel(Level.INFO);
        logger.setAdditivity(false);
    }

    public static Logger getJsonLogger() {
        return jsonLogger;
    }

    public static Logger getLogger() {
        return logger;
    }
}
