// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.code.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.github.nikyotensai.decompiler.main.DecompilerContext;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class ExceptionRangeCFG {

    private final List<BasicBlock> protectedRange; // FIXME: replace with set
    private BasicBlock handler;
    private List<String> exceptionTypes;
    private static final Joiner JOINER = Joiner.on(":");

    public ExceptionRangeCFG(List<BasicBlock> protectedRange, BasicBlock handler, List<String> exceptionType) {
        this.protectedRange = protectedRange;
        this.handler = handler;

        if (exceptionType != null) {
            this.exceptionTypes = new ArrayList<>(exceptionType);
        }
    }

    public boolean isCircular() {
        return protectedRange.contains(handler);
    }

    @Override
    public String toString() {
        String new_line_separator = DecompilerContext.getNewLineSeparator();
        StringBuilder buf = new StringBuilder();

        buf.append("exceptionType:");
        for (String exception_type : exceptionTypes) {
            buf.append(" ").append(exception_type);
        }
        buf.append(new_line_separator);

        buf.append("handler: ").append(handler.id).append(new_line_separator);
        buf.append("range: ");
        for (BasicBlock block : protectedRange) {
            buf.append(block.id).append(" ");
        }
        buf.append(new_line_separator);

        return buf.toString();
    }

    public BasicBlock getHandler() {
        return handler;
    }

    public void setHandler(BasicBlock handler) {
        this.handler = handler;
    }

    public List<BasicBlock> getProtectedRange() {
        return protectedRange;
    }

    public List<String> getExceptionTypes() {
        return this.exceptionTypes;
    }

    public void addExceptionType(String exceptionType) {
        if (this.exceptionTypes == null) {
            return;
        }

        if (exceptionType == null) {
            this.exceptionTypes = null;
        } else {
            this.exceptionTypes.add(exceptionType);
        }
    }

    public String getUniqueExceptionsString() {
        //return exceptionTypes != null ? exceptionTypes.stream().distinct().collect(Collectors.joining(":")) : null;
        if (exceptionTypes != null) {
            HashSet<String> set = Sets.newHashSet(exceptionTypes);
            return JOINER.join(set);
        } else {
            return null;
        }
    }
}