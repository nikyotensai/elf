package com.github.nikyotensai.elf.attach.common;

import com.taobao.middleware.logger.util.MessageUtil;

/**
 * @author cai.wen created on 2019/11/5 15:24
 */
public class ElfLoggerHelper {

    public static String formatMessage(String formatText, Object... args) {
        return MessageUtil.formatMessage(formatText, args);
    }
}
