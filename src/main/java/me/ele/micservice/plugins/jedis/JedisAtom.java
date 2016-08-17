package me.ele.micservice.plugins.jedis;

import redis.clients.jedis.Transaction;

/**
 * Created by frankliu on 15/8/21.
 */
public interface JedisAtom {
    void action(Transaction transaction);
}
