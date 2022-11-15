package com.github.nikyotensai.elf.server.proxy.communicate;

/**
 * @author zhenyu.nie created on 2019 2019/10/31 15:30
 */
public interface WritableListener {

    void onChange(boolean writable);
}
