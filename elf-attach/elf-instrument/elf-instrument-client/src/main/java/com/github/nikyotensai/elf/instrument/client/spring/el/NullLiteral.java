/*
 * Copyright 2002-2014 the original author or authors.
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
 * Expression language AST node that represents null.
 *
 * @author Andy Clement
 * @since 3.0
 */
class NullLiteral extends Literal {

    public NullLiteral(int pos) {
        super(null, pos);
        this.exitTypeDescriptor = "Ljava/lang/Object";
    }


    @Override
    public TypedValue getLiteralValue() {
        return TypedValue.NULL;
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public boolean isCompilable() {
        return true;
    }

    @Override
    public void generateCode(MethodVisitor mv, CodeFlow cf) {
        mv.visitInsn(ACONST_NULL);
        cf.pushDescriptor(this.exitTypeDescriptor);
    }

}
