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
 * Expression language AST node that represents a string literal.
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
class StringLiteral extends Literal {

    private final TypedValue value;


    public StringLiteral(String payload, int pos, String value) {
        super(payload, pos);
        value = value.substring(1, value.length() - 1);
        this.value = new TypedValue(value.replaceAll("''", "'").replaceAll("\"\"", "\""));
        this.exitTypeDescriptor = "Ljava/lang/String";
    }


    @Override
    public TypedValue getLiteralValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "'" + getLiteralValue().getValue() + "'";
    }

    @Override
    public boolean isCompilable() {
        return true;
    }

    @Override
    public void generateCode(MethodVisitor mv, CodeFlow cf) {
        mv.visitLdcInsn(this.value.getValue());
        cf.pushDescriptor(this.exitTypeDescriptor);
    }

}
