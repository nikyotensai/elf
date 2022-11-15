// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.struct.attr;

import java.io.IOException;

import com.github.nikyotensai.decompiler.struct.consts.ConstantPool;
import com.github.nikyotensai.decompiler.util.DataInputFullStream;

public class StructGenericSignatureAttribute extends StructGeneralAttribute {

    private String signature;

    @Override
    public void initContent(DataInputFullStream data, ConstantPool pool) throws IOException {
        int index = data.readUnsignedShort();
        signature = pool.getPrimitiveConstant(index).getString();
    }

    public String getSignature() {
        return signature;
    }
}
