// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.struct.attr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.nikyotensai.decompiler.modules.decompiler.exps.AnnotationExprent;
import com.github.nikyotensai.decompiler.struct.consts.ConstantPool;
import com.github.nikyotensai.decompiler.util.DataInputFullStream;

public class StructAnnotationParameterAttribute extends StructGeneralAttribute {

    private List<List<AnnotationExprent>> paramAnnotations;

    @Override
    public void initContent(DataInputFullStream data, ConstantPool pool) throws IOException {
        int len = data.readUnsignedByte();
        if (len > 0) {
            paramAnnotations = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                List<AnnotationExprent> annotations = StructAnnotationAttribute.parseAnnotations(pool, data);
                paramAnnotations.add(annotations);
            }
        } else {
            paramAnnotations = Collections.emptyList();
        }
    }

    public List<List<AnnotationExprent>> getParamAnnotations() {
        return paramAnnotations;
    }
}