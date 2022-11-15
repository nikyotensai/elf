/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package com.github.nikyotensai.decompiler.modules.decompiler.exps;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.nikyotensai.decompiler.main.collectors.BytecodeMappingTracer;
import com.github.nikyotensai.decompiler.util.InterpreterUtil;
import com.github.nikyotensai.decompiler.util.TextBuffer;

public class MonitorExprent extends Exprent {

    public static final int MONITOR_ENTER = 0;
    public static final int MONITOR_EXIT = 1;

    private final int monType;
    private Exprent value;

    public MonitorExprent(int monType, Exprent value, Set<Integer> bytecodeOffsets) {
        super(EXPRENT_MONITOR);
        this.monType = monType;
        this.value = value;

        addBytecodeOffsets(bytecodeOffsets);
    }

    @Override
    public Exprent copy() {
        return new MonitorExprent(monType, value.copy(), bytecode);
    }

    @Override
    public List<Exprent> getAllExprents() {
        List<Exprent> lst = new ArrayList<>();
        lst.add(value);
        return lst;
    }

    @Override
    public TextBuffer toJava(int indent, BytecodeMappingTracer tracer) {
        tracer.addMapping(bytecode);

        if (monType == MONITOR_ENTER) {
            return value.toJava(indent, tracer).enclose("synchronized(", ")");
        } else {
            return new TextBuffer();
        }
    }

    @Override
    public void replaceExprent(Exprent oldExpr, Exprent newExpr) {
        if (oldExpr == value) {
            value = newExpr;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof MonitorExprent)) return false;

        MonitorExprent me = (MonitorExprent) o;
        return monType == me.getMonType() &&
                InterpreterUtil.equalObjects(value, me.getValue());
    }

    public int getMonType() {
        return monType;
    }

    public Exprent getValue() {
        return value;
    }
}
