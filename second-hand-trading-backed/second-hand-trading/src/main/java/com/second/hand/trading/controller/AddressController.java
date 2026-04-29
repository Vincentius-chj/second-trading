package com.second.hand.trading.controller;

import com.second.hand.trading.enums.ErrorMsg;
import com.second.hand.trading.model.AddressModel;
import com.second.hand.trading.service.AddressService;
import com.second.hand.trading.vo.ResultVo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("info")
    public ResultVo getAddress(HttpServletRequest request,
                               @RequestParam(value = "id", required = false) Long id) {

        /**
         * 1.@Null 限制只能为null
         * @NotNull 限制必须不为null   对象不是null就行，其他的不保证。
         *
         * 2.@NotEmpty除了@NotNull之外还需要保证@Size(min=1)
         * 这也是一个注解，这里规定最小长度等于1，也就是类似于集合非空。
         *
         * 3.使用Spring Session替代Cookie
         *
         * 4.使用 @RequestParam 将请求参数绑定至方法参数
         *
         *  4.1 若required参数使用了该注解，则该参数默认是必须提供的，但你也可以把该参数标注为非必须的：
         *  只需要将 @RequestParam 注解的 required 属性设置为 false
         *
         *  4.2 这里使用的 required = false 是将请求的参数设置为 null ，
         *  所以方法里的参数需要为引用类型（Integer），如果使用的是基本类型（int）会出现错误
         */

        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }

        if (null == id) {
            return ResultVo.success(addressService.getAddressByUser(Long.valueOf(shUserId)));
        } else {
            return ResultVo.success(addressService.getAddressById(id, Long.valueOf(shUserId)));
        }
    }

    @PostMapping("add")
    public ResultVo addAddress(HttpServletRequest request,
                              @RequestBody AddressModel addressModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        addressModel.setUserId(Long.valueOf(shUserId));
        if (addressService.addAddress(addressModel)) {
            return ResultVo.success(addressModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @PostMapping("update")
    public ResultVo updateAddress(HttpServletRequest request,
                                  @RequestBody AddressModel addressModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        addressModel.setUserId(Long.valueOf(shUserId));
        if (addressService.updateAddress(addressModel)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @PostMapping("delete")
    public ResultVo deleteAddress(HttpServletRequest request,
                                  @RequestBody AddressModel addressModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        addressModel.setUserId(Long.valueOf(shUserId));
        if (addressService.deleteAddress(addressModel)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }
}