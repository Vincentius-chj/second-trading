package com.second.hand.trading.dao;

import com.second.hand.trading.model.IdleItemModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IdleItemDao {

    int deleteByPrimaryKey(Long id);

    int insert(IdleItemModel record);

    int insertSelective(IdleItemModel record);

    IdleItemModel selectByPrimaryKey(Long id);

    List<IdleItemModel> getAllIdleItem(Long userId);

    int countIdleItem(String findValue);

    int countIdleItemByLable(int idleLabel);

    int countIdleItemByStatus(int status);

    List<IdleItemModel> findIdleItem(@Param("findValue") String findValue, @Param("begin") int begin, @Param("nums") int nums);

    List<IdleItemModel> findIdleItem1(@Param("findValue") String findValue, @Param("status") int status, @Param("begin") int begin, @Param("nums") int nums);

    // 根据状态和搜索词查找闲置物品
    List<IdleItemModel> findIdleItemByStatus(@Param("findValue") String findValue, @Param("status") int status, @Param("begin") int begin, @Param("nums") int nums);

    // 根据状态和搜索词统计数量
    int countIdleItemByStatusAndFindValue(@Param("findValue") String findValue, @Param("status") int status);

    List<IdleItemModel> findIdleItemByLable(@Param("idleLabel") int idleLabel, @Param("begin") int begin, @Param("nums") int nums);

    List<IdleItemModel> getIdleItemByStatus(@Param("status") int status, @Param("begin") int begin, @Param("nums") int nums);

    // 多字段搜索闲置
    List<IdleItemModel> findIdleItemByMultiFields(@Param("idleName") String idleName, @Param("minPrice") String minPrice, @Param("maxPrice") String maxPrice, @Param("idleLabel") Integer idleLabel, @Param("idlePlace") String idlePlace, @Param("userNickname") String userNickname, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("begin") int begin, @Param("nums") int nums);

    // 多字段搜索闲置计数
    int countIdleItemByMultiFields(@Param("idleName") String idleName, @Param("minPrice") String minPrice, @Param("maxPrice") String maxPrice, @Param("idleLabel") Integer idleLabel, @Param("idlePlace") String idlePlace, @Param("userNickname") String userNickname, @Param("startTime") String startTime, @Param("endTime") String endTime);

    // 按状态多字段搜索闲置
    List<IdleItemModel> findIdleItemByMultiFieldsWithStatus(@Param("idleName") String idleName, @Param("minPrice") String minPrice, @Param("maxPrice") String maxPrice, @Param("idleLabel") Integer idleLabel, @Param("idlePlace") String idlePlace, @Param("userNickname") String userNickname, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("status") Integer status, @Param("begin") int begin, @Param("nums") int nums);

    // 按状态多字段搜索闲置计数
    int countIdleItemByMultiFieldsWithStatus(@Param("idleName") String idleName, @Param("minPrice") String minPrice, @Param("maxPrice") String maxPrice, @Param("idleLabel") Integer idleLabel, @Param("idlePlace") String idlePlace, @Param("userNickname") String userNickname, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("status") Integer status);

    int updateByPrimaryKeySelective(IdleItemModel record);

    int updateByPrimaryKey(IdleItemModel record);

    int decreaseStock(@Param("id") Long id, @Param("count") Integer count);

    int increaseStock(@Param("id") Long id, @Param("count") Integer count);

    List<IdleItemModel> findIdleByList(@Param("idList") List<Long> idList);
}