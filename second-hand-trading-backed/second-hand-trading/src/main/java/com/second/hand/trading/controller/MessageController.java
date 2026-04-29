package com.second.hand.trading.controller;

import com.second.hand.trading.enums.ErrorMsg;
import com.second.hand.trading.model.MessageModel;
import com.second.hand.trading.service.MessageService;
import com.second.hand.trading.vo.ResultVo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResultVo sendMessage(HttpServletRequest request,
                                @RequestBody MessageModel messageModel) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        messageModel.setUserId(Long.valueOf(shUserId));
        messageModel.setMessageType(0);
        messageModel.setCreateTime(new Date());
        if (messageService.addMessage(messageModel)) {
            return ResultVo.success(messageModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("/info")
    public ResultVo getMessage(@RequestParam Long id) {
        return ResultVo.success(messageService.getMessage(id));
    }

    @GetMapping("/idle")
    public ResultVo getAllIdleMessage(@RequestParam Long idleId) {
        return ResultVo.success(messageService.getAllIdleMessage(idleId));
    }

    @GetMapping("/my")
    public ResultVo getAllMyMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        return ResultVo.success(messageService.getAllMyMessage(Long.valueOf(shUserId)));
    }

    @GetMapping("/delete")
    public ResultVo deleteMessage(HttpServletRequest request,
                                  @RequestParam Long id) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        String shUserId = (String) session.getAttribute("shUserId");
        if (shUserId == null || shUserId.isEmpty()) {
            return ResultVo.fail(ErrorMsg.COOKIE_ERROR);
        }
        MessageModel dbMessage = messageService.getMessage(id);
        if (dbMessage == null) {
            return ResultVo.fail(ErrorMsg.PARAM_ERROR);
        }
        Long currentUserId = Long.valueOf(shUserId);
        boolean isSender = currentUserId.equals(dbMessage.getUserId());
        boolean isReceiver = dbMessage.getToUser() != null && currentUserId.equals(dbMessage.getToUser());
        if (!isSender && !isReceiver) {
            return ResultVo.fail(ErrorMsg.RESOURCE_FORBIDDEN);
        }
        if (messageService.deleteMessage(id)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }
}