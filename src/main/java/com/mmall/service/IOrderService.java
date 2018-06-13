package com.mmall.service;

import com.mmall.common.ServerResponse;

/**
 * @Auther gongfukang
 * @Date 2018/6/12 23:16
 */
public interface IOrderService {

    ServerResponse pay(Long orderNo, Integer userId, String path);
}
