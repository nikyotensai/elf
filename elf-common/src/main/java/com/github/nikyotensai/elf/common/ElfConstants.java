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

package com.github.nikyotensai.elf.common;

import java.io.File;

/**
 * @author zhenyu.nie created on 2018 2018/11/28 19:59
 */
public interface ElfConstants {

    String PROJECT_NAME = "ELF";

    String MAX_RUNNING_MS = "max.running.ms";

    String FILL_PID = "$$FILLPID$$";

    String FILL_DUMP_TARGET = "$$DUMPTARGET$$";

    String PID_PARAM = " -pid";

    String REQ_JAR_DEBUG = "jardebug";

    String REQ_JAR_CLASS_PATH = "jarclasspath";

    String REQ_DEBUG_ADD = "qdebugadd";

    String REQ_DEBUG_REMOVE = "qdebugremove";

    String REQ_DEBUG_SEARCH = "qdebugsearch";

    String REQ_MONITOR_ADD = "qmonitoradd";

    String REQ_MONITOR_SNAPSHOT = "qmonitorsnapshot";

    String REQ_JAR_INFO = "jarinfo";

    String REQ_APP_CONFIG = "appconfig";

    String REQ_APP_CONFIG_FILE = "appconfigfile";

    String REQ_AGENT_INFO = "agentinfopush";

    String ELF_COMMAND_THREAD_NAME = "elf-command-execute-daemon";

    String SPY_CLASSNAME = "com.github.nikyotensai.elf.instrument.spy.ElfSpys1";

    String CURRENT_VERSION = "0.0.1";

    String ELF_VERSION_LINE_PREFIX = "elf version:";

    String SHUTDOWN_COMMAND = "shutdown";

    String STOP_COMMAND = "stop";

    int MIN_AGENT_VERSION_SUPPORT_JOB_PAUSE = 12;

    String PROFILER_ROOT_PATH = System.getProperty("java.io.tmpdir") + File.separator + "elf-profiler";

    String PROFILER_ROOT_TEMP_PATH = PROFILER_ROOT_PATH + File.separator + "tmp";

    String PROFILER_ROOT_AGENT_PATH = PROFILER_ROOT_PATH + File.separator + "agent";

    String PROFILER_DIR_HEADER = "profilerDir";

    String PROFILER_NAME_HEADER = "profilerName";


}
