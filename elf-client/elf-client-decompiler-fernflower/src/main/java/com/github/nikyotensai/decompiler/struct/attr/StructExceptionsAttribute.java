// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.struct.attr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.nikyotensai.decompiler.struct.consts.ConstantPool;
import com.github.nikyotensai.decompiler.util.DataInputFullStream;

public class StructExceptionsAttribute extends StructGeneralAttribute {

    private List<Integer> throwsExceptions;

    @Override
    public void initContent(DataInputFullStream data, ConstantPool pool) throws IOException {
        int len = data.readUnsignedShort();
        if (len > 0) {
            throwsExceptions = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                throwsExceptions.add(data.readUnsignedShort());
            }
        } else {
            throwsExceptions = Collections.emptyList();
        }
    }

    public String getExcClassname(int index, ConstantPool pool) {
        return pool.getPrimitiveConstant(throwsExceptions.get(index)).getString();
    }

    public List<Integer> getThrowsExceptions() {
        return throwsExceptions;
    }
}
