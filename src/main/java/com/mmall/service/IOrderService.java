package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.Map;

/**
 * @Auther gongfukang
 * @Date 2018/6/12 23:16
 */
public interface IOrderService {

    ServerResponse pay(Long orderNo, Integer userId, String path);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);
}
