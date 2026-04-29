package com.second.hand.trading.dao;

import com.second.hand.trading.model.UserModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDao {

    int deleteByPrimaryKey(Long id);

    int insert(UserModel record);

    int insertSelective(UserModel record);

    UserModel userLogin(@Param("accountNumber") String accountNumber, @Param("userPassword") String userPassword);

    UserModel selectByPrimaryKey(Long id);

    Long selectByUserName(String nickname);

    List<UserModel> getUserList();

    List<UserModel> findUserByList(@Param("idList") List<Long> idList);

    List<UserModel> getNormalUser(@Param("begin") int begin, @Param("nums") int nums);

    List<UserModel> getBanUser(@Param("begin") int begin, @Param("nums") int nums);

    // 通过账户查找用户
    List<UserModel> getUserByNumber(@Param("searchValue") String searchValue, @Param("mode") int mode);

    // 通过多个字段搜索用户
    List<UserModel> getUserByMultiFields(@Param("id") String id, @Param("nickname") String nickname, @Param("accountNumber") String accountNumber, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("userStatus") int userStatus, @Param("begin") int begin, @Param("nums") int nums);

    // 通过多个字段统计用户数量
    int countUserByMultiFields(@Param("id") String id, @Param("nickname") String nickname, @Param("accountNumber") String accountNumber, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("userStatus") int userStatus);

    int countNormalUser();

    int countBanUser();

    int updateByPrimaryKeySelective(UserModel record);

    int updateByPrimaryKey(UserModel record);

    int updatePassword(@Param("newPassword") String newPassword,
                       @Param("oldPassword") String oldPassword,
                       @Param("id") Long id
    );

    int updateUserBackground(@Param("backgroundImg") String backgroundImg, @Param("id") Long id);
}