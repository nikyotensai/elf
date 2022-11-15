/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package com.github.nikyotensai.decompiler.modules.decompiler.stats;

import com.github.nikyotensai.decompiler.code.CodeConstants;
import com.github.nikyotensai.decompiler.code.Instruction;
import com.github.nikyotensai.decompiler.code.SimpleInstructionSequence;
import com.github.nikyotensai.decompiler.code.cfg.BasicBlock;
import com.github.nikyotensai.decompiler.main.DecompilerContext;
import com.github.nikyotensai.decompiler.main.collectors.BytecodeMappingTracer;
import com.github.nikyotensai.decompiler.main.collectors.CounterContainer;
import com.github.nikyotensai.decompiler.modules.decompiler.ExprProcessor;
import com.github.nikyotensai.decompiler.util.TextBuffer;

public class BasicBlockStatement extends Statement {

    // *****************************************************************************
    // private fields
    // *****************************************************************************

    private final BasicBlock block;

    // *****************************************************************************
    // constructors
    // *****************************************************************************

    public BasicBlockStatement(BasicBlock block) {

        type = Statement.TYPE_BASICBLOCK;

        this.block = block;

        id = block.id;
        CounterContainer coun = DecompilerContext.getCounterContainer();
        if (id >= coun.getCounter(CounterContainer.STATEMENT_COUNTER)) {
            coun.setCounter(CounterContainer.STATEMENT_COUNTER, id + 1);
        }

        Instruction instr = block.getLastInstruction();
        if (instr != null) {
            if (instr.group == CodeConstants.GROUP_JUMP && instr.opcode != CodeConstants.opc_goto) {
                lastBasicType = LASTBASICTYPE_IF;
            } else if (instr.group == CodeConstants.GROUP_SWITCH) {
                lastBasicType = LASTBASICTYPE_SWITCH;
            }
        }

        // monitorenter and monitorexits
        buildMonitorFlags();
    }

    // *****************************************************************************
    // public methods
    // *****************************************************************************

    @Override
    public TextBuffer toJava(int indent, BytecodeMappingTracer tracer) {
        TextBuffer tb = ExprProcessor.listToJava(varDefinitions, indent, tracer);
        tb.append(ExprProcessor.listToJava(exprents, indent, tracer));
        return tb;
    }

    @Override
    public Statement getSimpleCopy() {

        BasicBlock newblock = new BasicBlock(
                DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.STATEMENT_COUNTER));

        SimpleInstructionSequence seq = new SimpleInstructionSequence();
        for (int i = 0; i < block.getSeq().length(); i++) {
            seq.addInstruction(block.getSeq().getInstr(i).clone(), -1);
        }

        newblock.setSeq(seq);

        return new BasicBlockStatement(newblock);
    }

    // *****************************************************************************
    // getter and setter methods
    // *****************************************************************************

    public BasicBlock getBlock() {
        return block;
    }
}
