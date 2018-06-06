package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;

/**
 * @Auther gongfukang
 * @Date 2018/6/6 9:17
 */
public interface IUserService {
    ServiceResponse<User> login(String username, String password);

    ServiceResponse<String> register(User user);

    ServiceResponse<String> checkValid(String str, String type);

    ServiceResponse selectQuestion(String username);

    ServiceResponse<String> checkAnswer(String username, String question, String answer);
}
