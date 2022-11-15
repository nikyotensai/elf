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

package com.github.nikyotensai.elf.agent.task.cpujstack;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.sun.tools.attach.VirtualMachine;

import lombok.extern.slf4j.Slf4j;
import sun.tools.attach.HotSpotVirtualMachine;

/**
 * @author cai.wen
 * @since 19-1-22
 */
@Slf4j
public class JStackPidExecutor implements PidExecutor {


    @Override
    public String execute(int pid) {
        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = VirtualMachine.attach(String.valueOf(pid));
            HotSpotVirtualMachine hotSpotVirtualMachine = (HotSpotVirtualMachine) virtualMachine;
            return readJStackOutput(hotSpotVirtualMachine);
        } catch (Exception e) {
            log.error("run JStackPidExecutor error pid:{}", pid, e);
        } finally {
            if (virtualMachine != null) {
                try {
                    virtualMachine.detach();
                } catch (IOException e) {
                    log.error("virtualMachine detach error pid:{}", pid, e);
                }
            }
        }
        return "";
    }

    private String readJStackOutput(HotSpotVirtualMachine hotSpotVirtualMachine) throws IOException {
        try (InputStream inputStream = hotSpotVirtualMachine.remoteDataDump(new String[0])) {
            byte[] bytes = ByteStreams.toByteArray(inputStream);
            return new String(bytes, Charsets.UTF_8);
        }
    }
}
