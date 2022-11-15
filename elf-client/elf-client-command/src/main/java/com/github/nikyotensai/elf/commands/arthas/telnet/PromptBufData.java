package com.github.nikyotensai.elf.commands.arthas.telnet;

import static com.github.nikyotensai.elf.commands.arthas.telnet.CommunicateUtil.MAX_PROMPT_LENGTH;

import com.google.common.base.Charsets;

/**
 * @author zhenyu.nie created on 2019 2019/10/12 17:06
 */
class PromptBufData {

    public final byte[] data = new byte[MAX_PROMPT_LENGTH - 1];

    private int length = 0;

    public int getLength() {
        return length;
    }

    public void clear() {
        length = 0;
    }

    public void reset(byte[] bytes, int start, int count) {
        if (count == 0) {
            clear();
            return;
        }

        System.arraycopy(bytes, start, data, 0, count);
        length = count;
    }

    public void add(byte[] bytes, int start, int count) {
        if (count == 0) {
            return;
        }

        System.arraycopy(bytes, start, data, length, count);
        length += count;
    }

    public byte[] copy() {
        byte[] bytes = new byte[length];
        System.arraycopy(data, 0, bytes, 0, length);
        return bytes;
    }

    @Override
    public String toString() {
        return "PromptBufData{" +
                "data=" + new String(data, 0, length, Charsets.UTF_8) +
                '}';
    }
}
