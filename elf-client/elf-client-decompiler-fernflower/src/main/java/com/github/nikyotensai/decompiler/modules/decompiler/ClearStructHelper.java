// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.modules.decompiler;

import java.util.LinkedList;

import com.github.nikyotensai.decompiler.modules.decompiler.stats.RootStatement;
import com.github.nikyotensai.decompiler.modules.decompiler.stats.Statement;


public class ClearStructHelper {

    public static void clearStatements(RootStatement root) {

        LinkedList<Statement> stack = new LinkedList<>();
        stack.add(root);

        while (!stack.isEmpty()) {

            Statement stat = stack.removeFirst();

            stat.clearTempInformation();

            stack.addAll(stat.getStats());
        }
    }
}
