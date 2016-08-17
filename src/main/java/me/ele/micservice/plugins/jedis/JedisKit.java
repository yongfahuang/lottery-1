package me.ele.micservice.plugins.jedis;

import me.ele.micservice.utils.SerializableKit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.util.SafeEncoder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by frankliu on 15/8/21.
 */
public class JedisKit {

    private static JedisPool pool;

    public static void init(JedisPool pool) {
        JedisKit.pool = pool;
    }

    public static List<Object> tx(JedisAtom jedisAtom) {
        Jedis jedis = pool.getResource();
        Transaction trans = jedis.multi();
        jedisAtom.action(trans);
        return trans.exec();
    }

    public static List<Object> tx(JedisAtom jedisAtom, String ... keys) {
        Jedis jedis = pool.getResource();
        jedis.watch(keys);
        Transaction trans = jedis.multi();
        jedisAtom.action(trans);
        return trans.exec();
    }

    public static <T> T call(JedisAction<T> jedisAction) {
        T result = null;
        Jedis jedis = pool.getResource();
        try {
            result = jedisAction.action(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != jedis)
                jedis.close();
        }
        return result;
    }

    public static <T extends Serializable> T get(final String key) {
        return call(jedis -> {
            Object result = null;
            byte[] retVal = jedis.get(SafeEncoder.encode(key));
            if (null != retVal) {
                try {
                    result = SerializableKit.toObject(retVal);
                } catch (Exception e) {
                    result = SafeEncoder.encode(retVal);
                }
            }
            return (T) result;
        });
    }

    public static boolean set(final String key, final Serializable value) {
        return call(jedis -> {
            String retVal;
            if (value instanceof String) {
                retVal = jedis.set(key, (String) value);
            } else {
                retVal = jedis.set(SafeEncoder.encode(key), SerializableKit.toByteArray(value));
            }
            return "OK".equalsIgnoreCase(retVal);
        });
    }

    public static boolean set(final String key, final Serializable value, final int seconds) {
        return call(jedis -> {
            byte[] bytes;
            if (value instanceof String) {
                bytes = SafeEncoder.encode((String) value);
            } else {
                bytes = SerializableKit.toByteArray(value);
            }
            String retVal = jedis.setex(SafeEncoder.encode(key), seconds, bytes);
            return "OK".equalsIgnoreCase(retVal);
        });
    }

    public static long del(final String... keys) {
        return call(jedis -> {
            byte[][] encodeKeys = new byte[keys.length][];
            for (int i = 0; i < keys.length; i++)
                encodeKeys[i] = SafeEncoder.encode(keys[i]);
            return jedis.del(encodeKeys);
        });
    }

    public static long incrementAndGet(final String key) {
        return call(jedis -> jedis.incr(key));
    }

    public static long decrementAndGet(final String key) {
        return call(jedis -> jedis.decr(key));
    }

    public static boolean exists(String key) {
        return call(jedis -> jedis.exists(key));
    }

    public static long expire(String key, int timeout) {
        return call(jedis -> jedis.expire(key, timeout));
    }
    public static Set<String> keys(String key) {
        return call(jedis -> jedis.keys(key));
    }

    public static long ttl(String key) {
        return call(jedis -> jedis.ttl(key));
    }

    public static String hget(String key, String field) {
        return call(jedis -> jedis.hget(key, field));
    }

    public static long hset(String key, String field, String value) {
        return call(jedis -> jedis.hset(key, field, value));
    }

    public static long hdel(String key, String... fields) {
        return call(jedis -> jedis.hdel(key, fields));
    }

    public static Set<String> hkeys(String key) {
        return call(jedis -> jedis.hkeys(key));
    }

    public static long setnx(String key, String value) {
        return call(jedis -> jedis.setnx(key, value));
    }

    public static Long lpush(String key, String... value) {
        return call(jedis -> jedis.lpush(key, value));
    }

    public static String rpop(String key) {
        return call(jedis -> jedis.rpop(key));
    }

    public static Long lrem(String key, String value) {
        return call(jedis -> jedis.lrem(key, 0, value));
    }

    public static Long rpush(String key, String... value) {
        return call(jedis -> jedis.rpush(key, value));
    }

    public static List<String> lrange(String key, long start, long end) {
        return call(jedis -> jedis.lrange(key, start, end));
    }

    public static Long llen(String key) {
        return call(jedis -> jedis.llen(key));
    }

    public static long hincrBy(String key, String field, long value) {
        return call(jedis -> jedis.hincrBy(key, field, value));
    }

    public static boolean hmset(String key, Map<String, String> hash) {
        return call(jedis -> {
            String retVal = jedis.hmset(key, hash);
            return "OK".equalsIgnoreCase(retVal);
        });
    }

    public static List<String> hmget(String key, String... fields) {
        return call(jedis -> jedis.hmget(key, fields));
    }

    public static Set<String> hkets(String key) {
        return call(jedis -> jedis.hkeys(key));
    }

    public static boolean hexist(String key, String field) {
        return call(jedis -> jedis.hexists(key, field));
    }

    public static List<String> hvals(String key) {
        return call(jedis -> jedis.hvals(key));
    }

    public static Map<String, String> hgetall(String key) {
        return call(jedis -> jedis.hgetAll(key));
    }
}
