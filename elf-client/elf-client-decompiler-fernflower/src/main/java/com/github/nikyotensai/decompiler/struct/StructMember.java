// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.struct;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.nikyotensai.decompiler.code.CodeConstants;
import com.github.nikyotensai.decompiler.struct.attr.StructGeneralAttribute;
import com.github.nikyotensai.decompiler.struct.attr.StructLocalVariableTableAttribute;
import com.github.nikyotensai.decompiler.struct.attr.StructLocalVariableTypeTableAttribute;
import com.github.nikyotensai.decompiler.struct.consts.ConstantPool;
import com.github.nikyotensai.decompiler.util.DataInputFullStream;

public class StructMember {

    protected int accessFlags;
    protected Map<String, StructGeneralAttribute> attributes;


    public int getAccessFlags() {
        return accessFlags;
    }

    public <T extends StructGeneralAttribute> T getAttribute(StructGeneralAttribute.Key<T> attribute) {
        //noinspection unchecked
        return (T) attributes.get(attribute.getName());
    }

    public boolean hasAttribute(StructGeneralAttribute.Key<?> attribute) {
        return attributes.containsKey(attribute.getName());
    }

    public boolean hasModifier(int modifier) {
        return (accessFlags & modifier) == modifier;
    }

    public boolean isSynthetic() {
        return hasModifier(CodeConstants.ACC_SYNTHETIC) || hasAttribute(StructGeneralAttribute.ATTRIBUTE_SYNTHETIC);
    }

    protected Map<String, StructGeneralAttribute> readAttributes(DataInputFullStream in, ConstantPool pool) throws IOException {
        int length = in.readUnsignedShort();

        Map<String, StructGeneralAttribute> attributes = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            int nameIndex = in.readUnsignedShort();
            String name = pool.getPrimitiveConstant(nameIndex).getString();

            StructGeneralAttribute attribute = readAttribute(in, pool, name);

            if (attribute != null) {
                if (StructGeneralAttribute.ATTRIBUTE_LOCAL_VARIABLE_TABLE.getName().equals(name) && attributes.containsKey(name)) {
                    // merge all variable tables
                    StructLocalVariableTableAttribute table = (StructLocalVariableTableAttribute) attributes.get(name);
                    table.add((StructLocalVariableTableAttribute) attribute);
                } else if (StructGeneralAttribute.ATTRIBUTE_LOCAL_VARIABLE_TYPE_TABLE.getName().equals(name) && attributes.containsKey(name)) {
                    // merge all variable tables
                    StructLocalVariableTypeTableAttribute table = (StructLocalVariableTypeTableAttribute) attributes.get(name);
                    table.add((StructLocalVariableTypeTableAttribute) attribute);
                } else {
                    attributes.put(attribute.getName(), attribute);
                }
            }
        }

        return attributes;
    }

    protected StructGeneralAttribute readAttribute(DataInputFullStream in, ConstantPool pool, String name) throws IOException {
        StructGeneralAttribute attribute = StructGeneralAttribute.createAttribute(name);
        int length = in.readInt();
        if (attribute == null) {
            in.discard(length);
        } else {
            attribute.initContent(in, pool);
        }
        return attribute;
    }
}