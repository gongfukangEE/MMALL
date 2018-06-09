package com.mmall.controller.protal;

import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Auther gongfukang
 * @Date 2018/6/9 20:01
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    IProductService iProductService;

    /**
     * 用户根据 ID 查找商品
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        return iProductService.getProductDetail(productId);
    }

    /**
     * 用户搜索商品，需要分页
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "categoryId", required = false) Integer categoryId,
                               @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               @RequestParam(value = "orderBy", defaultValue = "") String orderBy) {
        return iProductService.getProductByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);
    }
}
