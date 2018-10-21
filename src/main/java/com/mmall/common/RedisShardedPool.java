package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther gongfukang
 * @Date 7/7 20:55
 * Redis 分片 连接池
 */
public class RedisShardedPool {
    private static ShardedJedisPool pool;      // ShardedJedis 连接池
    private static Integer maxTotal =
            Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));    // 最大连接数
    private static Integer maxIdle =
            Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));     // 在 jedispool 中最大的 idle 状态（空闲的）jedis 实例个数
    private static Integer minIdle =
            Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));     // 在 jedispool 中最大的 idle 状态（空闲的）jedis 实例个数

    private static Boolean testOnBorrow =
            Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));        // 在 borrow 一个 jedis 实例的时候，是否要进行验证操作，如果赋值为 true，则得到的 jedis 实例肯定是可用的
    private static Boolean testOnReturn =
            Boolean.parseBoolean(PropertiesUtil.getProperty("redis.return.borrow", "true"));        // 在 return 一个 jedis 实例的时候，是否要进行验证操作，如果赋值为 true，则放回 jedispool 的 jedis 实例是可以用的

    private static String redis1IP = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    private static String redis2IP = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true); //连接耗尽的时候时候是否阻塞

        JedisShardInfo info1 = new JedisShardInfo(redis1IP, redis1Port, 1000 * 2);
        JedisShardInfo info2 = new JedisShardInfo(redis2IP, redis2Port, 1000 * 2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>(2);

        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, ShardedJedis.DEFAULT_KEY_TAG_PATTERN);

    }

    // 类加载到 jvm 时直接初始化连接池
    static {
        initPool();
    }

    public static ShardedJedis getJedis() {
        return pool.getResource();
    }

    public static void returnBrokenResource(ShardedJedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    /**
     * 放回连接池
     */
    public static void returnResource(ShardedJedis jedis) {
        pool.returnResource(jedis);
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();
        for (int i = 0; i < 20; i++) {
            jedis.set("key" + i, "value" + i);
        }
        returnResource(jedis);
        System.out.println("program is end");
    }
}
