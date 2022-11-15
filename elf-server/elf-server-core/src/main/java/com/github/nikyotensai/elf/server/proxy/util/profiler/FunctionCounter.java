package com.github.nikyotensai.elf.server.proxy.util.profiler;

import java.util.Objects;

import com.github.nikyotensai.elf.common.profiler.method.FunctionInfo;

/**
 * @author cai.wen created on 19-11-24
 */
public class FunctionCounter implements Comparable<FunctionCounter> {

    private final FunctionInfo functionInfo;

    private long count = 0;

    FunctionCounter(FunctionInfo functionInfo) {
        this.functionInfo = functionInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionCounter that = (FunctionCounter) o;
        return Objects.equals(functionInfo, that.functionInfo);
    }

    @Override
    public int hashCode() {
        return functionInfo.hashCode();
    }

    public long getCount() {
        return count;
    }

    public void add(long delay) {
        count += delay;
    }

    public FunctionInfo getFunctionInfo() {
        return functionInfo;
    }

    @Override
    public int compareTo(FunctionCounter o) {
        return (int) (this.count - o.count);
    }

    @Override
    public String toString() {
        return "FunctionCounter{" +
                "functionInfo=" + functionInfo +
                ", count=" + count +
                '}';
    }
}