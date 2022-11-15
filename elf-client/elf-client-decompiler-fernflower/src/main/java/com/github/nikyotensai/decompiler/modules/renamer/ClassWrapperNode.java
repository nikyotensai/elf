// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.modules.renamer;

import java.util.ArrayList;
import java.util.List;

import com.github.nikyotensai.decompiler.struct.StructClass;

public class ClassWrapperNode {

    private final StructClass classStruct;
    private final List<ClassWrapperNode> subclasses = new ArrayList<>();

    public ClassWrapperNode(StructClass cl) {
        this.classStruct = cl;
    }

    public void addSubclass(ClassWrapperNode node) {
        subclasses.add(node);
    }

    public StructClass getClassStruct() {
        return classStruct;
    }

    public List<ClassWrapperNode> getSubclasses() {
        return subclasses;
    }
}