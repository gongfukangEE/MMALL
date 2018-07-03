package com.mmall.common;

import redis.clients.jedis.JedisPool;

/**
 * @Auther gongfukang
 * @Date 7/3 16:50
 */
public class RedisPool {
    private static JedisPool pool;      // edis 连接池
    private static Integer maxTotal;    // 最大连接数
    private static Integer maxIdle;     // 在 jedispool 中最大的 idle 状态（空闲的）jedis 实例个数
    private static Integer minIdle;     // 在 jedispool 中最大的 idle 状态（空闲的）jedis 实例个数
    private static Boolean testOnBorrow;        // 在 borrow 一个 jedis 实例的时候，是否要进行验证操作，如果赋值为 true，则得到的 jedis 实例肯定是可用的
    private static Boolean testOnReturn;        // 在 return 一个 jedis 实例的时候，是否要进行验证操作，如果赋值为 true，则放回 jedispool 的 jedis 实例是可以用的

}
