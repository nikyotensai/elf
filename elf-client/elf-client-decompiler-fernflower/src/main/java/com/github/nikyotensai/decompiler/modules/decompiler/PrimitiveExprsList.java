// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.modules.decompiler;

import java.util.ArrayList;
import java.util.List;

import com.github.nikyotensai.decompiler.modules.decompiler.exps.Exprent;

public class PrimitiveExprsList {

    private final List<Exprent> lstExprents = new ArrayList<>();

    private ExprentStack stack = new ExprentStack();

    public PrimitiveExprsList() {
    }

    public PrimitiveExprsList copyStack() {
        PrimitiveExprsList prlst = new PrimitiveExprsList();
        prlst.setStack(stack.clone());
        return prlst;
    }

    public List<Exprent> getLstExprents() {
        return lstExprents;
    }

    public ExprentStack getStack() {
        return stack;
    }

    public void setStack(ExprentStack stack) {
        this.stack = stack;
    }
}
