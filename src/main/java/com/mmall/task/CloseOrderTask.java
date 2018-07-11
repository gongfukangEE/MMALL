package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @Auther gongfukang
 * @Date 7/9 22:26
 * 定时关单
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;

    /**
     * 在关闭(调用 shutdown 方法) Tomcat 之前调用的方法
     * 1、如果要关闭的锁特别多，那就耗时很久  2、如果直接 kill tomcat 那就没法调用执行
     */
    @PreDestroy
    public void delLock() {
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }

    //@Scheduled(cron = "0 */1 * * * ?")  //每个 1 分钟的整数倍
    public void closeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        // iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

    //@Scheduled(cron = "0 */1 * * * ?")  //每个 1 分钟的整数倍
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");

        // 分布式锁的超时时间
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));

        // 存在死锁问题，如果在获取锁之后，执行 closeOrder 之前，tomcat 被关闭，会发生死锁。A: delLock
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            // 如果返回值是 1 表示设置成功，获取锁成功
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("没有获得分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }

        log.info("关闭订单定时任务结束");
    }

    // 加时间戳 双重检查
    //@Scheduled(cron = "0 */1 * * * ?")  //每个 1 分钟的整数倍
    public void closeOrderTaskV3() {
        log.info("关闭订单定时任务启动");

        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            // 未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                // 锁时间过期失效了（可以获取锁）
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
                // 再次用当前时间戳 getset 返回给定的 key 的旧值，根据旧值判断，是否可以获取锁
                // 当 key 没有旧值的时候，即 key 不存在的时候，返回 nil 获取锁
                // 这里 set 一个新的 value 值，获取旧的值
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(lockValueStr, getSetResult))) {
                    // 真正获取到锁
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                } else {
                    log.info("没有获取到分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            } else {
                // 旧的锁还没有失效
                log.info("没有获取到分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }

        log.info("关闭订单定时任务结束");
    }

    // Redisson
    @Scheduled(cron = "0 */1 * * * ?")  //每个 1 分钟的整数倍
    public void closeOrderTaskV4() {

        log.info("关闭订单定时任务启动");

        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);

        boolean getLock = false;
        try {
            if (getLock = lock.tryLock(2, 5, TimeUnit.SECONDS)) {
                log.info("Redisson 获取分布式锁，LockName: {}, ThreadName: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.hour", "2"));
                //iOrderService.closeOrder(hour);
            } else {
                log.info("Redisson 没有获取分布式锁，LockName: {}, ThreadName: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redisson 分布式锁获取异常", e);
        } finally {
            if (!getLock) {
                return;
            }
            lock.unlock();
            log.info("Redisson 分布式锁释放锁");
        }

        log.info("关闭订单定时任务结束");
    }

    /**
     * 设置锁的有效期
     */
    private void closeOrder(String lockName) {
        RedisShardedPoolUtil.expire(lockName, 5);  // 有效期 5 秒，防止死锁，线上 5 秒
        log.info("获取: {}, ThreadName: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        // iOrderService.closeOrder(hour);
        // 如果执行完成，还没有到超时时间，要及时释放锁
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放: {}, ThreadName: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        log.info("-------------------------------------------------------------------------");
    }
}
