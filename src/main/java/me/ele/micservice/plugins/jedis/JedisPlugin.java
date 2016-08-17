package me.ele.micservice.plugins.jedis;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import me.ele.micservice.utils.ResourceKit;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;

import java.util.Map;
import java.util.Set;

/**
 * Created by frankliu on 15/8/21.
 */
public class JedisPlugin implements IPlugin {

    public JedisPool pool;
    private String config = "redis.properties";

    private String host = "localhost";
    private int port = 6379;
    private int timeout = 2000;
    private String password;
    private int maxtotal = 512;
    private int maxidle = 512;
    private long maxwaitmillis = 5000;
    private int minidle = 64;
    private boolean testwhileidle = true;
    private boolean testonreturn = false;
    private boolean testonborrow = true;

    public JedisPlugin() {
    }

    public JedisPlugin(String host) {
        this.host = host;
    }

    public JedisPlugin(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public JedisPlugin(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    public JedisPlugin config(String config) {
        this.config = config;
        return this;
    }

    @Override
    public boolean start() {

        Map<String, String> map = ResourceKit.readProperties(config);
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            parseSetting(entry.getKey(), entry.getValue().trim());
        }
        JedisShardInfo shardInfo = new JedisShardInfo(host, port, timeout);
        if (StrKit.notBlank(password)) {
            shardInfo.setPassword(password);
        }
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        setPoolConfig(poolConfig);
        pool = new JedisPool(poolConfig, shardInfo.getHost(), shardInfo.getPort(), shardInfo.getSoTimeout(), shardInfo.getPassword());
        JedisKit.init(pool);
        return true;
    }

    @Override
    public boolean stop() {
        try {
            pool.destroy();
        } catch (Exception ex) {
            System.err.println("Cannot properly close Jedis pool:" + ex);
        }
        pool = null;
        return true;
    }

    private void setPoolConfig(JedisPoolConfig poolConfig) {
        poolConfig.setMaxTotal(maxtotal);
        poolConfig.setMaxIdle(maxidle);
        poolConfig.setMaxWaitMillis(maxwaitmillis);
        poolConfig.setMinIdle(minidle);
        poolConfig.setTestWhileIdle(testwhileidle);
        poolConfig.setTestOnReturn(testonreturn);
        poolConfig.setTestOnBorrow(testonborrow);
    }

    private void parseSetting(String key, String value) {
        if ("timeout".equalsIgnoreCase(key)) {
            timeout = Integer.valueOf(value);
        } else if ("password".equalsIgnoreCase(key)) {
            password = value;
        } else if ("host".equalsIgnoreCase(key)) {
            host = value;
        } else if ("maxtotal".equalsIgnoreCase(key)) {
            maxtotal = Integer.valueOf(value);
        } else if ("maxidle".equalsIgnoreCase(key)) {
            maxidle = Integer.valueOf(value);
        } else if ("maxwaitmillis".equalsIgnoreCase(key)) {
            maxwaitmillis = Integer.valueOf(value);
        } else if ("minidle".equalsIgnoreCase(key)) {
            minidle = Integer.valueOf(value);
        } else if ("testwhileidle".equalsIgnoreCase(key)) {
            testwhileidle = Boolean.getBoolean(value);
        } else if ("testonreturn".equalsIgnoreCase(key)) {
            testonreturn = Boolean.getBoolean(value);
        } else if ("testonborrow".equalsIgnoreCase(key)) {
            testonborrow = Boolean.getBoolean(value);
        }
    }
}
