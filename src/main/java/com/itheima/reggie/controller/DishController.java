package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entiy.Category;
import com.itheima.reggie.entiy.Dish;
import com.itheima.reggie.entiy.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/*
 * 商品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /*
     * 新增菜品
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /*
     * 菜品信息分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String pageName){
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(pageName!=null,Dish::getName,pageName);
        lqw.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,lqw);

        //对象拷贝  DishDto中的records属性不需要进行赋值
        //因为records属性为list集合，所拷贝的pageInfo和dishDtoPage所支持的records集合的泛型不同，需要手动进行处理
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        //getRecords()返回pageInfo中的Dish对象的list集合
        List<Dish> records = pageInfo.getRecords();
        //通过stream()的方式对records进行处理
        List<DishDto> list = records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            //此时获得的dishDto属性为空，使用BeanUtils为dishDto拷贝属性的值
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId(); //每个item（菜品）的分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category!=null) {
                String categoryName = category.getName();   //通过每个分类对象得到分类名字
                //将获得到的分类名字赋值给dishDto的categoryName属性
                dishDto.setCategoryName(categoryName);

            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /*
     * 根据id查询菜品信息和对应的口味信息
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /*
     * 新增菜品
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);


        return R.success("修改菜品成功");
    }

//    /*
//     * 根据条件查询对应的菜品
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        //构造查询条件
//        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<Dish>();
//        lqw.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        lqw.eq(Dish::getStatus,1);  //Status 是1表示启售状态，0表示停售
//        //排序条件
//        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(lqw);
//        return R.success(list);
//    }

    /*
     * 根据条件查询对应的菜品
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<Dish>();
        lqw.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        lqw.eq(Dish::getStatus,1);  //Status 是1表示启售状态，0表示停售
        //排序条件
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(lqw);

        List<DishDto> dishDtoList = list.stream().map((item)->{
            DishDto dishDto = new DishDto();
            //此时获得的dishDto属性为空，使用BeanUtils为dishDto拷贝属性的值
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper();
            lqw2.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lqw2);
            dishDto.setFlavors(dishFlavors);

            return dishDto;
        }).collect(Collectors.toList());



        return R.success(dishDtoList);
    }

}
