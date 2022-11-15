/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package com.github.nikyotensai.decompiler.modules.decompiler.exps;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.nikyotensai.decompiler.code.CodeConstants;
import com.github.nikyotensai.decompiler.main.ClassesProcessor;
import com.github.nikyotensai.decompiler.main.DecompilerContext;
import com.github.nikyotensai.decompiler.main.collectors.BytecodeMappingTracer;
import com.github.nikyotensai.decompiler.main.rels.MethodWrapper;
import com.github.nikyotensai.decompiler.modules.decompiler.ExprProcessor;
import com.github.nikyotensai.decompiler.modules.decompiler.vars.CheckTypesResult;
import com.github.nikyotensai.decompiler.struct.attr.StructExceptionsAttribute;
import com.github.nikyotensai.decompiler.struct.attr.StructGeneralAttribute;
import com.github.nikyotensai.decompiler.struct.gen.VarType;
import com.github.nikyotensai.decompiler.struct.match.MatchEngine;
import com.github.nikyotensai.decompiler.struct.match.MatchNode;
import com.github.nikyotensai.decompiler.util.InterpreterUtil;
import com.github.nikyotensai.decompiler.util.TextBuffer;

public class ExitExprent extends Exprent {

    public static final int EXIT_RETURN = 0;
    public static final int EXIT_THROW = 1;

    private final int exitType;
    private Exprent value;
    private final VarType retType;

    public ExitExprent(int exitType, Exprent value, VarType retType, Set<Integer> bytecodeOffsets) {
        super(EXPRENT_EXIT);
        this.exitType = exitType;
        this.value = value;
        this.retType = retType;

        addBytecodeOffsets(bytecodeOffsets);
    }

    @Override
    public Exprent copy() {
        return new ExitExprent(exitType, value == null ? null : value.copy(), retType, bytecode);
    }

    @Override
    public CheckTypesResult checkExprTypeBounds() {
        CheckTypesResult result = new CheckTypesResult();

        if (exitType == EXIT_RETURN && retType.type != CodeConstants.TYPE_VOID) {
            result.addMinTypeExprent(value, VarType.getMinTypeInFamily(retType.typeFamily));
            result.addMaxTypeExprent(value, retType);
        }

        return result;
    }

    @Override
    public List<Exprent> getAllExprents() {
        List<Exprent> lst = new ArrayList<>();
        if (value != null) {
            lst.add(value);
        }
        return lst;
    }

    @Override
    public TextBuffer toJava(int indent, BytecodeMappingTracer tracer) {
        tracer.addMapping(bytecode);

        if (exitType == EXIT_RETURN) {
            TextBuffer buffer = new TextBuffer("return");

            if (retType.type != CodeConstants.TYPE_VOID) {
                buffer.append(' ');
                ExprProcessor.getCastedExprent(value, retType, buffer, indent, false, tracer);
            }

            return buffer;
        } else {
            MethodWrapper method = (MethodWrapper) DecompilerContext.getProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
            ClassesProcessor.ClassNode node = ((ClassesProcessor.ClassNode) DecompilerContext.getProperty(DecompilerContext.CURRENT_CLASS_NODE));

            if (method != null && node != null) {
                StructExceptionsAttribute attr = method.methodStruct.getAttribute(StructGeneralAttribute.ATTRIBUTE_EXCEPTIONS);

                if (attr != null) {
                    String classname = null;

                    for (int i = 0; i < attr.getThrowsExceptions().size(); i++) {
                        String exClassName = attr.getExcClassname(i, node.classStruct.getPool());
                        if ("java/lang/Throwable".equals(exClassName)) {
                            classname = exClassName;
                            break;
                        } else if ("java/lang/Exception".equals(exClassName)) {
                            classname = exClassName;
                        }
                    }

                    if (classname != null) {
                        VarType exType = new VarType(classname, true);
                        TextBuffer buffer = new TextBuffer("throw ");
                        ExprProcessor.getCastedExprent(value, exType, buffer, indent, false, tracer);
                        return buffer;
                    }
                }
            }

            return value.toJava(indent, tracer).prepend("throw ");
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
        if (!(o instanceof ExitExprent)) return false;

        ExitExprent et = (ExitExprent) o;
        return exitType == et.getExitType() &&
                InterpreterUtil.equalObjects(value, et.getValue());
    }

    public int getExitType() {
        return exitType;
    }

    public Exprent getValue() {
        return value;
    }

    public VarType getRetType() {
        return retType;
    }

    // *****************************************************************************
    // IMatchable implementation
    // *****************************************************************************

    @Override
    public boolean match(MatchNode matchNode, MatchEngine engine) {
        if (!super.match(matchNode, engine)) {
            return false;
        }

        Integer type = (Integer) matchNode.getRuleValue(MatchProperties.EXPRENT_EXITTYPE);
        return type == null || this.exitType == type;
    }
}
