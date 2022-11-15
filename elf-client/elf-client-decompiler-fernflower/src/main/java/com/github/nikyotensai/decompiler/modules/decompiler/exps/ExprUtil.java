// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.modules.decompiler.exps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.nikyotensai.decompiler.code.CodeConstants;
import com.github.nikyotensai.decompiler.main.ClassesProcessor.ClassNode;
import com.github.nikyotensai.decompiler.main.DecompilerContext;
import com.github.nikyotensai.decompiler.main.extern.IFernflowerPreferences;
import com.github.nikyotensai.decompiler.main.rels.ClassWrapper;
import com.github.nikyotensai.decompiler.main.rels.MethodWrapper;
import com.github.nikyotensai.decompiler.modules.decompiler.vars.VarVersionPair;

public class ExprUtil {

    public static List<VarVersionPair> getSyntheticParametersMask(String className, String descriptor, int parameters) {
        ClassNode node = DecompilerContext.getClassProcessor().getMapRootClasses().get(className);
        return node != null ? getSyntheticParametersMask(node, descriptor, parameters) : null;
    }

    public static List<VarVersionPair> getSyntheticParametersMask(ClassNode node, String descriptor, int parameters) {
        List<VarVersionPair> mask = null;

        ClassWrapper wrapper = node.getWrapper();
        if (wrapper != null) {
            // own class
            MethodWrapper methodWrapper = wrapper.getMethodWrapper(CodeConstants.INIT_NAME, descriptor);
            if (methodWrapper == null) {
                if (DecompilerContext.getOption(IFernflowerPreferences.IGNORE_INVALID_BYTECODE)) {
                    return null;
                }
                throw new RuntimeException("Constructor " + node.classStruct.qualifiedName + "." + CodeConstants.INIT_NAME + descriptor + " not found");
            }
            mask = methodWrapper.synthParameters;
        } else if (parameters > 0 && node.type == ClassNode.CLASS_MEMBER && (node.access & CodeConstants.ACC_STATIC) == 0) {
            // non-static member class
            mask = new ArrayList<>(Collections.<VarVersionPair>nCopies(parameters, null));
            mask.set(0, new VarVersionPair(-1, 0));
        }

        return mask;
    }
}