package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Auther gongfukang
 * @Date 2018/6/8 16:20
 */
public interface IFileService {

    String upload(MultipartFile file, String path);
}
