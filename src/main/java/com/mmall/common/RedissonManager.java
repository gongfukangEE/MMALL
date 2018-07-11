package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Auther gongfukang
 * @Date 7/11 16:51
 */
@Component
@Slf4j
public class RedissonManager {

    private Config config = new Config();

    private Redisson redisson = null;

    public Redisson getRedisson() {
        return redisson;
    }

    private static String redis1IP = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    private static String redis2IP = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    @PostConstruct
    private void init() {
        config.useSingleServer().setAddress(new  StringBuilder().append(redis1IP).append(":").append(redis1Port).toString());
        try {
            redisson = (Redisson) Redisson.create(config);
            log.info("初始化 Redisson 结束");
        } catch (Exception e) {
            log.error("redisson init error", e);
        }
    }
}
