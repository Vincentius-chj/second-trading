package com.second.hand.trading.dao;

import com.second.hand.trading.model.OrderModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderDao {

    int deleteByPrimaryKey(Long id);

    int insert(OrderModel record);

    int insertSelective(OrderModel record);

    OrderModel selectByPrimaryKey(Long id);

    List<OrderModel> getMyOrder(Long userId);

    List<OrderModel> getAllOrder(@Param("begin") int begin, @Param("nums") int nums);

    List<OrderModel> getOrderByNumber(@Param("searchValue") String searchValue, @Param("begin") int begin, @Param("nums") int nums);

    OrderModel selectByOrderNumber(@Param("orderNumber") String orderNumber);

    // 多字段搜索订单
    List<OrderModel> findOrderByMultiFields(@Param("orderNumber") String orderNumber, @Param("idleName") String idleName, @Param("minPrice") String minPrice, @Param("maxPrice") String maxPrice, @Param("buyerNickname") String buyerNickname, @Param("sellerNickname") String sellerNickname, @Param("orderStatus") Integer orderStatus, @Param("paymentStatus") Integer paymentStatus, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("begin") int begin, @Param("nums") int nums);

    // 多字段搜索订单计数
    int countOrderByMultiFields(@Param("orderNumber") String orderNumber, @Param("idleName") String idleName, @Param("minPrice") String minPrice, @Param("maxPrice") String maxPrice, @Param("buyerNickname") String buyerNickname, @Param("sellerNickname") String sellerNickname, @Param("orderStatus") Integer orderStatus, @Param("paymentStatus") Integer paymentStatus, @Param("startTime") String startTime, @Param("endTime") String endTime);

    int countAllOrder();


    List<OrderModel> findOrderByIdleIdList(@Param("idleIdList") List<Long> idleIdList);

    int updateByPrimaryKeySelective(OrderModel record);

    int updateByPrimaryKey(OrderModel record);
}