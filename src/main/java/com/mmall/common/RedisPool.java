package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Auther gongfukang
 * @Date 7/3 16:50
 */
public class RedisPool {
    private static JedisPool pool;      // jedis 连接池
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));    // 最大连接数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));     // 在 jedispool 中最大的 idle 状态（空闲的）jedis 实例个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));     // 在 jedispool 中最大的 idle 状态（空闲的）jedis 实例个数

    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));        // 在 borrow 一个 jedis 实例的时候，是否要进行验证操作，如果赋值为 true，则得到的 jedis 实例肯定是可用的
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.return.borrow", "true"));        // 在 return 一个 jedis 实例的时候，是否要进行验证操作，如果赋值为 true，则放回 jedispool 的 jedis 实例是可以用的

    private static String redisIP = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));

    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true); //连接耗尽的时候时候是否阻塞

        pool = new JedisPool(config, redisIP, redisPort, 1000 * 2);
    }

    // 类加载到 jvm 时直接初始化连接池
    static {
        initPool();
    }

    public static Jedis getJedis() {
        return pool.getResource();
    }

    public static void returnBrokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        Jedis jedis = pool.getResource();
        jedis.set("testkey", "testvalue");
        returnResource(jedis);
        pool.destroy();     //临时调用，销毁连接池的所有连接

        System.out.println("program is end");
    }
}
