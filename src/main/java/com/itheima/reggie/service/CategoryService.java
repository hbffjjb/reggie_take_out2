package com.itheima.reggie.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entiy.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
