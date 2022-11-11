package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entiy.Category;
import com.itheima.reggie.entiy.Dish;
import com.itheima.reggie.entiy.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetMealService setMealService;
    /*
     * 根据id删除分类，删除之前需要进行判断
     */
    @Override
    public void remove(Long id) {
        //查询当前分类是否关联菜品，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper();
        lqw.eq(Dish::getCategoryId,id);
        int count = dishService.count(lqw);
        if (count>0){
            //已经关联，抛出一个业务异常
            throw new CustomException("当前分类项关联了菜品，不能删除");
        }

        //查询当前分类是否关联套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(Setmeal::getCategoryId, id);
        int count2 = setMealService.count(lqw2);
        if (count2 > 0) {
            //已经关联，抛出一个业务异常
            throw new CustomException("当前分类项关联了套餐，不能删除");
        }
        //正常删除分类
        super.removeById(id);
    }
}
