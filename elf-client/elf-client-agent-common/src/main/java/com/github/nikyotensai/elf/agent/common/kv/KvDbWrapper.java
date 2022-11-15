package com.github.nikyotensai.elf.agent.common.kv;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.client.common.store.ElfStore;

/**
 * @author leix.xie
 * @since 2020/5/9 18:06
 */
public class KvDbWrapper implements KvDb {

    private static final Logger LOG = LoggerFactory.getLogger(KvDbWrapper.class);
    private static final int DEFAULT_TTL = (int) TimeUnit.DAYS.toSeconds(3);

    private static final String SQLITE = "sqlite";

    private static final int DEFAULT_MAX_COMPACTIONS = 3;

    private final KvDb kvdb;

    public KvDbWrapper() {
        kvdb = new SQLiteStoreImpl(ElfStore.getStorePath(SQLITE), DEFAULT_TTL);
    }

    @Override
    public String get(String key) {
        return kvdb.get(key);
    }

    @Override
    public void put(String key, String value) {
        kvdb.put(key, value);
    }

    @Override
    public void putBatch(Map<String, String> data) {
        kvdb.putBatch(data);
    }
}
