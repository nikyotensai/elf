package com.github.nikyotensai.elf.server.proxy.util.profiler;

import java.util.List;

import com.github.nikyotensai.elf.common.profiler.method.FunctionInfo;

/**
 * @author cai.wen created on 19-11-24
 */
public class CallStackCounter {

    private final List<FunctionInfo> functionInfos;

    private final long count;

    public CallStackCounter(List<FunctionInfo> funcInfos, long count) {
        this.functionInfos = funcInfos;
        this.count = count;
    }

    public List<FunctionInfo> getFunctionInfos() {
        return functionInfos;
    }

    public long getCount() {
        return count;
    }
}
