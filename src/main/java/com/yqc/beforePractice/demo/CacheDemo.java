package com.yqc.beforePractice.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 简单的缓存系统
 * 2015.10.27
 *
 * @author Yqngqc
 */
public class CacheDemo {
    private Map<String, Object> cache = new HashMap<>();
    private ReadWriteLock rw = new ReentrantReadWriteLock();

    public static void main(String[] args) {

    }

    public /*synchronized*/ Object getData(String key) {
        rw.readLock().lock();
        Object value = null;
        try {
            if (value == null) {
                rw.readLock().unlock();
                rw.writeLock().lock();
                try {
                    if (value == null) {
                        value = "aaa"; // 实际上去数据库查询
                    }
                } finally {
                    rw.writeLock().unlock();
                }
                rw.readLock().lock();
            }
            value = cache.get(key);
        } finally {
            rw.readLock().unlock();
        }
        return value;
    }
}
