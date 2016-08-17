package me.ele.micservice.plugins.jedis;

import redis.clients.jedis.Jedis;

/**
 * Created by frankliu on 15/8/21.
 */
public interface JedisAction<T> {
    T action(Jedis jedis);
}
