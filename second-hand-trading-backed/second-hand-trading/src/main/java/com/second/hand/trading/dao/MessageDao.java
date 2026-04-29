package com.second.hand.trading.dao;

import com.second.hand.trading.model.MessageModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageDao {

    int deleteByPrimaryKey(Long id);

    int insert(MessageModel record);

    int insertSelective(MessageModel record);

    MessageModel selectByPrimaryKey(Long id);

    List<MessageModel> getMyMessage(Long userId);

    List<MessageModel> getIdleMessage(Long idleId);

    List<MessageModel> getIdleReview(Long idleId);

    MessageModel getOrderReview(Long orderId);

    MessageModel findReviewByOrderIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);

    int updateByPrimaryKeySelective(MessageModel record);

    int updateByPrimaryKey(MessageModel record);
}