package com.github.nikyotensai.elf.attach.arthas.util;

import com.github.nikyotensai.elf.common.CodeProcessResponse;
import com.github.nikyotensai.elf.common.TypeResponse;

/**
 * @author cai.wen created on 2019/11/5 14:40
 */
public class TypeResponseResult {

    public static <T> TypeResponse<T> create(T t, String type) {
        CodeProcessResponse<T> response = new CodeProcessResponse<>();
        TypeResponse<T> typeResponse = new TypeResponse<>();
        typeResponse.setType(type);
        typeResponse.setData(response);
        response.setData(t);
        return typeResponse;
    }
}
