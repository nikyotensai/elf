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


import org.objectweb.asm.MethodVisitor;

/**
 * Expression language AST node that represents an integer literal.
 *
 * @author Andy Clement
 * @since 3.0
 */
class IntLiteral extends Literal {

    private final TypedValue value;


    public IntLiteral(String payload, int pos, int value) {
        super(payload, pos);
        this.value = new TypedValue(value);
        this.exitTypeDescriptor = "I";
    }


    @Override
    public TypedValue getLiteralValue() {
        return this.value;
    }

    @Override
    public boolean isCompilable() {
        return true;
    }

    @Override
    public void generateCode(MethodVisitor mv, CodeFlow cf) {
        int intValue = (Integer) this.value.getValue();
        if (intValue == -1) {
            // Not sure we can get here because -1 is OpMinus
            mv.visitInsn(ICONST_M1);
        } else if (intValue >= 0 && intValue < 6) {
            mv.visitInsn(ICONST_0 + intValue);
        } else {
            mv.visitLdcInsn(intValue);
        }
        cf.pushDescriptor(this.exitTypeDescriptor);
    }

}
