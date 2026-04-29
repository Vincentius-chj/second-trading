package com.second.hand.trading.controller;

import com.second.hand.trading.enums.ErrorMsg;
import com.second.hand.trading.model.IdleItemModel;
import com.second.hand.trading.service.IdleItemService;
import com.second.hand.trading.vo.ResultVo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("idle")
public class IdleItemController {

    @Autowired
    private IdleItemService idleItemService;

    @PostMapping("add")
    public ResultVo addIdleItem(HttpServletRequest request,
                                @RequestBody IdleItemModel idleItemModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        idleItemModel.setUserId(Long.valueOf(shUserId));
        if (idleItemModel.getIdleLabel() != null && idleItemModel.getIdleLabel() == 5) {
            idleItemModel.setIdleStock(1);
        } else if (idleItemModel.getIdleStock() == null || idleItemModel.getIdleStock() <= 0) {
            return ResultVo.fail(ErrorMsg.IDLE_STOCK_INVALID);
        }
        idleItemModel.setIdleStatus((byte) 3); // 待审核状态
        idleItemModel.setReleaseTime(new Date());
        if (idleItemService.addIdleItem(idleItemModel)) {
            return ResultVo.success(idleItemModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("info")
    public ResultVo getIdleItem(@RequestParam Long id) {
        return ResultVo.success(idleItemService.getIdleItem(id));
    }

    @GetMapping("all")
    public ResultVo getAllIdleItem(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        return ResultVo.success(idleItemService.getAllIdelItem(Long.valueOf(shUserId)));
    }

    @GetMapping("find")
    public ResultVo findIdleItem(@RequestParam(value = "findValue", required = false) String findValue,
                                 @RequestParam(value = "page", required = false) Integer page,
                                 @RequestParam(value = "nums", required = false) Integer nums) {
        if (null == findValue) {
            findValue = "";
        }
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }
        return ResultVo.success(idleItemService.findIdleItem(findValue, p, n));
    }

    @GetMapping("lable")
    public ResultVo findIdleItemByLable(@RequestParam(value = "idleLabel", required = true) Integer idleLabel,
                                        @RequestParam(value = "page", required = false) Integer page,
                                        @RequestParam(value = "nums", required = false) Integer nums) {
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }
        return ResultVo.success(idleItemService.findIdleItemByLable(idleLabel, p, n));
    }

    @PostMapping("update")
    public ResultVo updateIdleItem(HttpServletRequest request,
                                  @RequestBody IdleItemModel idleItemModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        if (idleItemModel.getId() == null) {
            return ResultVo.fail(ErrorMsg.PARAM_ERROR);
        }
        IdleItemModel dbIdleItem = idleItemService.getIdleItem(idleItemModel.getId());
        if (dbIdleItem == null) {
            return ResultVo.fail(ErrorMsg.PARAM_ERROR);
        }
        Long currentUserId = Long.valueOf(shUserId);
        if (!currentUserId.equals(dbIdleItem.getUserId())) {
            return ResultVo.fail(ErrorMsg.RESOURCE_FORBIDDEN);
        }
        idleItemModel.setUserId(Long.valueOf(shUserId));
        if (idleItemModel.getIdleLabel() != null && idleItemModel.getIdleLabel() == 5) {
            idleItemModel.setIdleStock(1);
        }
        if (idleItemModel.getIdleStock() != null && idleItemModel.getIdleStock() <= 0) {
            return ResultVo.fail(ErrorMsg.IDLE_STOCK_INVALID);
        }
        if (idleItemService.updateIdleItem(idleItemModel)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }
}