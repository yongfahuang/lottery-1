package me.ele.micservice.utils;

import me.ele.micservice.plugins.jedis.JedisKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by frankliu on 15/10/8.
 */
public class LockKit {

    private static final Logger logger = LoggerFactory.getLogger(LockKit.class);
    private static final String LOCKED = "TRUE";
    private static final long ONE_MILLI_NANOS = 1000000L;
    //默认超时时间(毫秒)
    private static final long DEFAULT_TIME_OUT = 3000;
    private static final Random r = new Random();
    //锁的超时时间(秒),过期删除
    private static final int EXPIRE = 30;
    //锁前缀
    private static final String PREFIX = "lock:";

    private String key;
    private boolean locked = false;

    public LockKit(String key) {
        this.key = PREFIX + key;
    }

    public boolean lock(long timeout) {
        long nano = System.nanoTime();
        timeout *= ONE_MILLI_NANOS;
        try {
            while ((System.nanoTime() - nano) < timeout){
                if (JedisKit.setnx(key, LOCKED) == 1) {
                    JedisKit.expire(key, EXPIRE);
                    locked = true;
                    return locked;
                }
                Thread.sleep(3, r.nextInt(500));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean lock() {
        return lock(DEFAULT_TIME_OUT);
    }

    public void unlock() {
        if (locked)
            JedisKit.del(key);
    }

}
