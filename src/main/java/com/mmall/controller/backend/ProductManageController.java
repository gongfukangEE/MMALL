package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Auther gongfukang
 * @Date 2018/6/7 20:35
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    /**
     * 保存商品
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(Product product) {
        // 全部通过拦截器检查权限
        return iProductService.saveOrUpdateProduct(product);
    }

    /**
     * 后台更新产品状态
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(Integer productId, Integer status) {
        // 全部通过拦截器检查权限
        return iProductService.setSaleStatus(productId, status);
    }

    /**
     * 后台获取产品详情
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(Integer productId) {
        // 全部通过拦截器检查权限
        return iProductService.manageProductDetail(productId);
    }

    /**
     * 后台获取商品列表
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        // 全部通过拦截器检查权限
        return iProductService.getProductList(pageNum, pageSize);
    }

    /**
     * 后台搜索商品
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        // 全部通过拦截器检查权限
        return iProductService.searchProduct(productName, productId, pageNum, pageSize);
    }

    /**
     * 后台文件上传
     */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request) {
        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetFileName = iFileService.upload(file, path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
        Map fileMap = Maps.newHashMap();
        fileMap.put("uri", targetFileName);
        fileMap.put("url", url);
        return ServerResponse.createBySuccess(fileMap);
    }

    /**
     * 后台图片富文本上传
     */
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        /* 富文本中对于返回值需按照 simditor 要求进行返回
            {
                "success":true/false,
                "msg":"error message"
                "file_path":"[real file path]"
            }
        */

        Map resultMap = Maps.newHashMap();
        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetFileName = iFileService.upload(file, path);

        if (StringUtils.isBlank(targetFileName)) {
            resultMap.put("success",false);
            resultMap.put("msg","上传失败");
            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return resultMap;
        }

        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
        resultMap.put("success", true);
        resultMap.put("msg", "上传成功");
        resultMap.put("file_path", url);

        return resultMap;
    }
}
