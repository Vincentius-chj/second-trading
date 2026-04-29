package com.second.hand.trading.service;

import com.second.hand.trading.model.UserModel;
import com.second.hand.trading.vo.PageVo;

public interface UserService {

    /**
     * 获取某个用户的公开信息
     *
     * @param id
     * @return
     */
    UserModel getUser(Long id);

    /**
     * 登录接口
     *
     * @param accountNumber
     * @param userPassword
     * @return
     */
    UserModel userLogin(String accountNumber, String userPassword);

    /**
     * 注册接口
     *
     * @param userModel
     * @return
     */
    boolean userSignIn(UserModel userModel);

    /**
     * 更新用户信息
     *
     * @param userModel
     * @return
     */
    boolean updateUserInfo(UserModel userModel);

    /**
     * 修改密码
     *
     * @param newPassword
     * @param oldPassword
     * @param id
     * @return
     */
    boolean updatePassword(String newPassword, String oldPassword, Long id);

    boolean updateUserBackground(String backgroundImg, Long id);

    PageVo<UserModel> getUserByStatus(int status, int page, int nums);


    /**
     * 通过用户名查询用户id
     */
    Long getUserId(String nickname);

    /**
     * 通过用户的账号查找用户信息
     *
     * @return
     */

    PageVo<UserModel> getUserByNumber(String searchValue, int mode);

    /**
     * 通过多个字段搜索用户信息（ID、昵称、账号、注册时间范围）
     *
     * @param id
     * @param nickname
     * @param accountNumber
     * @param startTime
     * @param endTime
     * @param mode
     * @return
     */
    PageVo<UserModel> getUserByMultiFields(String id, String nickname, String accountNumber, String startTime, String endTime, int mode, int page, int nums);
}
