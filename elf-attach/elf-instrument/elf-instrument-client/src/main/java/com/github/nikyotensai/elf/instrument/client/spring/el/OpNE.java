/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nikyotensai.elf.instrument.client.spring.el;


import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Implements the not-equal operator.
 *
 * @author Andy Clement
 * @since 3.0
 */
class OpNE extends Operator {

    public OpNE(int pos, SpelNodeImpl... operands) {
        super("!=", pos, operands);
        this.exitTypeDescriptor = "Z";
    }


    @Override
    public BooleanTypedValue getValueInternal(ExpressionState state) throws EvaluationException {
        Object left = getLeftOperand().getValueInternal(state).getValue();
        Object right = getRightOperand().getValueInternal(state).getValue();
        this.leftActualDescriptor = CodeFlow.toDescriptorFromObject(left);
        this.rightActualDescriptor = CodeFlow.toDescriptorFromObject(right);
        return BooleanTypedValue.forValue(
                !equalityCheck(state.getEvaluationContext(), left, right));
    }

    // This check is different to the one in the other numeric operators (OpLt/etc)
    // because we allow simple object comparison
    @Override
    public boolean isCompilable() {
        SpelNodeImpl left = getLeftOperand();
        SpelNodeImpl right = getRightOperand();
        if (!left.isCompilable() || !right.isCompilable()) {
            return false;
        }

        String leftDesc = left.exitTypeDescriptor;
        String rightDesc = right.exitTypeDescriptor;
        DescriptorComparison dc = DescriptorComparison.checkNumericCompatibility(leftDesc,
                rightDesc, this.leftActualDescriptor, this.rightActualDescriptor);
        return (!dc.areNumbers || dc.areCompatible);
    }

    @Override
    public void generateCode(MethodVisitor mv, CodeFlow cf) {
        cf.loadEvaluationContext(mv);
        String leftDesc = getLeftOperand().exitTypeDescriptor;
        String rightDesc = getRightOperand().exitTypeDescriptor;
        boolean leftPrim = CodeFlow.isPrimitive(leftDesc);
        boolean rightPrim = CodeFlow.isPrimitive(rightDesc);

        cf.enterCompilationScope();
        getLeftOperand().generateCode(mv, cf);
        cf.exitCompilationScope();
        if (leftPrim) {
            CodeFlow.insertBoxIfNecessary(mv, leftDesc.charAt(0));
        }
        cf.enterCompilationScope();
        getRightOperand().generateCode(mv, cf);
        cf.exitCompilationScope();
        if (rightPrim) {
            CodeFlow.insertBoxIfNecessary(mv, rightDesc.charAt(0));
        }

        String operatorClassName = Operator.class.getName().replace('.', '/');
        String evaluationContextClassName = EvaluationContext.class.getName().replace('.', '/');
        mv.visitMethodInsn(INVOKESTATIC, operatorClassName, "equalityCheck",
                "(L" + evaluationContextClassName + ";Ljava/lang/Object;Ljava/lang/Object;)Z", false);

        // Invert the boolean
        Label notZero = new Label();
        Label end = new Label();
        mv.visitJumpInsn(IFNE, notZero);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, end);
        mv.visitLabel(notZero);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(end);

        cf.pushDescriptor("Z");
    }

}
