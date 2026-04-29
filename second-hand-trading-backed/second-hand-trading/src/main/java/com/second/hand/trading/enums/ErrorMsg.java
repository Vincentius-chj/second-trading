package com.second.hand.trading.enums;

import lombok.Getter;

/**
 * @author myl
 * 错误信息枚举类
 */
@Getter
public enum ErrorMsg {

    ACCOUNT_EXIT("用户已存在"),
    ACCOUNT_LEN("手机号长度不符合要求"),
    ACCOUNT_Ban("账号已被封禁"),
    ACCOUNT_NOT_EXIT("用户不存在"),
    PASSWORD_IS_NOT_SAME("密码不一致"),
    PASSWORD_RESET_ERROR("修改密码失败"),
    EMAIL_SEND_ERROR("邮件发送失败 请重试"),
    PARAM_ERROR("参数错误"),
    SYSTEM_ERROR("系统错误"),
    REGISTER_ERROR("注册失败"),
    FILE_TYPE_ERROR("文件类型错误 请选择.jpg或.png"),
    FILE_UPLOAD_ERROR("文件上传失败"),
    FILE_NOT_EXIT("文件不存在"),
    FILE_DOWNLOAD_ERROR("文件下载异常"),
    FILE_SIZE_ERROR("文件过大"),
    OPERAT_FREQUENCY("操作频繁 稍后重试"),
    MISSING_PARAMETER("缺少参数"),
    COOKIE_ERROR("请先登录"),
    EMAIL_LOGIN_ERROR("登录失败 账号或密码错误"),
    JSON_READ_ERROR("json参数解析错误"),
    FORM_NUMBER_ERROR("表单id错误"),
    REPEAT_COMMIT_ERROR("请勿重复提交"),
    COMMIT_FAIL_ERROR("提交失败"),
    FAVORITE_EXIT("收藏已存在"),
    FILE_UPLOAD_FAILED("文件上传失败"),
    SELF_PURCHASE_ERROR("不能购买自己发布的闲置物品"),
    IDLE_STOCK_INVALID("库存数量必须大于0"),
    IDLE_STOCK_NOT_ENOUGH("库存不足或商品已下架"),
    ORDER_CREATE_FAILED("下单失败，请检查库存或商品状态"),
    RESOURCE_FORBIDDEN("无权限操作该资源"),
    ORDER_STATE_INVALID("订单状态流转不合法"),
    PAY_AMOUNT_MISMATCH("支付金额校验失败"),
    REVIEW_NOT_ALLOWED("当前订单不可评价"),
    REVIEW_ALREADY_EXISTS("该订单已评价"),
    REVIEW_SCORE_INVALID("评分必须在1到5之间"),
    OFFLINE_REASON_REQUIRED("请填写下架原因");

    private String msg;

    ErrorMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
