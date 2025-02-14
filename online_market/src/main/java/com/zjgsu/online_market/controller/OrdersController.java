package com.zjgsu.online_market.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zjgsu.online_market.common.annotations.LoginRequired;
import com.zjgsu.online_market.common.dto.PageDto;
import com.zjgsu.online_market.common.lang.Result;
import com.zjgsu.online_market.entity.Orders;
import com.zjgsu.online_market.service.IOrdersService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xjj
 * @since 2021-09-09
 * 订单情况：0  等待中
 *         1  交易中
 *         2  交易成功
 *         3  交易失败
 * 货物：0
 **/
@RestController
public class OrdersController {
    @Autowired
    private IOrdersService ordersService;


    @ApiOperation("创建订单")
    @PostMapping("/orders/insert")
    public Result insertOrders(@Validated @NotNull(message = "空对象") Orders orders){
        return ordersService.insertOrders(orders);
    }

    @ApiOperation("查询所有订单")
    @GetMapping("/orders")
    public Result pageList(@Validated PageDto pageDto) {
        return Result.success(ordersService.getOrderPage(pageDto.getCurrentpage(),pageDto.getSize()));
    }
    @ApiOperation("查询历史订单")
    @GetMapping("/orders/history")
    public Result historyPageList(@Validated PageDto pageDto) {
        if (pageDto == null) return Result.fail("空参数");
        IPage iPage = ordersService.getHistoryListPage(pageDto.getCurrentpage(),pageDto.getSize());
        return Result.success(iPage);
    }
    @ApiOperation("获取未接受的订单")
    @GetMapping("/orders/list")
    public Result getList(){
        return Result.success(ordersService.list(new QueryWrapper<Orders>().eq("status",0)));
    }

    @ApiOperation("接受订单")
    @LoginRequired(required = true)
    @PostMapping("/orders/accept")
    public Result accept(@NotNull(message = "oid为空") Long oid,@NotNull(message = "gid为空") Long gid) {
        int res = ordersService.acceptOrder(oid,gid);
        if (res == 200)
        {
            return Result.success("成功");
        }
        if (res == 1){
            return Result.fail("售空",1);
        }else {
            return Result.fail("订单无法更改",2);
        }
    }

    @ApiOperation("拒绝订单")
    @LoginRequired(required = true)
    @PostMapping("/orders/reject/{oid}")
    public Result reject(@PathVariable(name = "oid") @NotNull(message = "oid为空") Long oid) {
        if (ordersService.rejectById(oid) == false)
            return Result.fail(400,"订单不存在",1);
        else
            return Result.success(oid);
    }

    @ApiOperation("完成订单")
    @LoginRequired(required = true)
    @PostMapping("/orders/success")
    public Result success(@NotNull(message = "oid为空") Long oid,@NotNull(message = "oid为空") Long gid) {
        int flag = ordersService.successById(oid,gid);
        if (flag == 1) {
            return Result.fail(406,"商品已售空",1);
        }else if (flag == 2) {
            return Result.fail(406,"商品下架中",2);
        }else if (flag == 3)
        {
            return Result.fail(406,"无此商品",3);
        } else {
            return Result.success(oid);
        }
    }

}
