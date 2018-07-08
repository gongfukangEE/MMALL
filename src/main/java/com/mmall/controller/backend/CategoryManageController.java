package com.mmall.controller.backend;

import com.mmall.common.ServerResponse;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @Auther gongfukang
 * @Date 2018/6/7 10:55
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 增加节点
     */
    @RequestMapping(value = "add_category.do")
    @ResponseBody
    public ServerResponse addCategory(String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        // 全部通过拦截器检查权限
        return iCategoryService.addCategory(categoryName, parentId);
    }

    /**
     * 更新名字
     */
    @RequestMapping(value = "set_Category_Name.do")
    @ResponseBody
    public ServerResponse setCategoryName(Integer categoryId, String categoryName) {
        // 全部通过拦截器检查权限
        return iCategoryService.updateCategoryName(categoryId, categoryName);
    }

    /**
     * 平级查询子节点
     */
    @RequestMapping(value = "get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        // 全部通过拦截器检查权限
        return iCategoryService.getChildrenParallelCategory(categoryId);
    }

    /**
     * 递归查询子节点
     */
    @RequestMapping(value = "get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        // 全部通过拦截器检查权限
        return iCategoryService.selectCategoryAndChildrenById(categoryId);
    }
}
