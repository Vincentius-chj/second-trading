package com.second.hand.trading.service.impl;

import com.second.hand.trading.dao.IdleItemDao;
import com.second.hand.trading.dao.OrderDao;
import com.second.hand.trading.dao.OrderAddressDao;
import com.second.hand.trading.dao.UserDao;
import com.second.hand.trading.model.IdleItemModel;
import com.second.hand.trading.model.OrderAddressModel;
import com.second.hand.trading.model.OrderModel;
import com.second.hand.trading.model.UserModel;
import com.second.hand.trading.service.OrderService;
import com.second.hand.trading.utils.OrderTask;
import com.second.hand.trading.utils.OrderTaskHandler;
import com.second.hand.trading.vo.PageVo;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderDao orderDao;

    @Resource
    private IdleItemDao idleItemDao;

    @Resource
    private OrderAddressDao orderAddressDao;

    private static HashMap<Integer, ReentrantLock> lockMap = new HashMap<>();

    static {
//        ReentrantLock lock=new ReentrantLock(true);
        for (int i = 0; i < 100; i++) {
            lockMap.put(i, new ReentrantLock(true));
        }
    }

    public boolean addOrder(OrderModel orderModel) {
        IdleItemModel idleItemModel = idleItemDao.selectByPrimaryKey(orderModel.getIdleId());
        if (idleItemModel == null) {
            return false;
        }
        if (idleItemModel.getIdleStock() == null) {
            IdleItemModel patchIdleItem = new IdleItemModel();
            patchIdleItem.setId(idleItemModel.getId());
            patchIdleItem.setIdleStock(1);
            idleItemDao.updateByPrimaryKeySelective(patchIdleItem);
            idleItemModel.setIdleStock(1);
        }
        if (idleItemModel.getIdleStatus() != 1) {
            return false;
        }
        if (orderModel.getOrderCount() == null || orderModel.getOrderCount() <= 0) {
            return false;
        }
        if (idleItemModel.getIdleStock() == null || idleItemModel.getIdleStock() < orderModel.getOrderCount()) {
            return false;
        }
        
        // 验证用户不能购买自己发布的闲置物品
        if (orderModel.getUserId() != null && idleItemModel.getUserId() != null && 
            orderModel.getUserId().equals(idleItemModel.getUserId())) {
            return false;
        }

        orderModel.setOrderPrice(idleItemModel.getIdlePrice().multiply(BigDecimal.valueOf(orderModel.getOrderCount())));

        int key = (int) (orderModel.getIdleId() % 100);
        ReentrantLock lock = lockMap.get(key);
        boolean flag;
        try {
            lock.lock();
            flag = addOrderHelp(orderModel);
        } finally {
            lock.unlock();
        }
        return flag;
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean addOrderHelp(OrderModel orderModel) {
        IdleItemModel idleItemModel = idleItemDao.selectByPrimaryKey(orderModel.getIdleId());
        if (idleItemModel == null || idleItemModel.getIdleStatus() != 1) {
            return false;
        }
        if (idleItemModel.getIdleStock() == null) {
            IdleItemModel patchIdleItem = new IdleItemModel();
            patchIdleItem.setId(idleItemModel.getId());
            patchIdleItem.setIdleStock(1);
            idleItemDao.updateByPrimaryKeySelective(patchIdleItem);
            idleItemModel.setIdleStock(1);
        }
        if (idleItemModel.getIdleStock() == null || idleItemModel.getIdleStock() < orderModel.getOrderCount()) {
            return false;
        }

        if (idleItemDao.decreaseStock(orderModel.getIdleId(), orderModel.getOrderCount()) == 1) {
            if (orderDao.insert(orderModel) == 1) {
                // 15分钟未支付则取消订单
                OrderModel cancelOrderModel = new OrderModel();
                cancelOrderModel.setId(orderModel.getId());
                cancelOrderModel.setOrderStatus((byte) 4); // 设置为已取消状态
                OrderTaskHandler.addOrder(new OrderTask(cancelOrderModel, 15 * 60));
                return true;
            } else {
                throw new RuntimeException("创建订单失败");
            }
        }
        return false;
    }

    /**
     * 获取订单信息，同时获取对应的闲置信息
     *
     * @param id
     * @return
     */
    public OrderModel getOrder(Long id) {
        OrderModel orderModel = orderDao.selectByPrimaryKey(id);
        if (orderModel == null) {
            return null;
        }
        orderModel.setIdleItem(idleItemDao.selectByPrimaryKey(orderModel.getIdleId()));
        return orderModel;
    }

    /**
     * 根据订单号，查询订单
     *
     * @return
     */
    @Override
    public OrderModel getOrderByNumber(String orderNumber) {
        // 从DAO层获取通过订单号查询的方法
        return orderDao.selectByOrderNumber(orderNumber);
    }
    
    @Override
    public PageVo<OrderModel> findOrderByNumber(String searchValue, int page, int nums) {
        List<OrderModel> list = orderDao.getOrderByNumber(searchValue, (page - 1) * nums, nums);
        attachOrderAddress(list);

        if (list.size() > 0) {
            List<Long> idleIdList = new ArrayList<>();
            for (OrderModel i : list) {
                idleIdList.add(i.getIdleId());
            }
            List<IdleItemModel> idleItemModelList = idleItemDao.findIdleByList(idleIdList);
            Map<Long, IdleItemModel> map = new HashMap<>();
            for (IdleItemModel idle : idleItemModelList) {
                map.put(idle.getId(), idle);
            }
            for (OrderModel i : list) {
                i.setIdleItem(map.get(i.getIdleId()));
            }
        }

        return new PageVo<OrderModel>(list, 1);
    }

    @Override
    public PageVo<OrderModel> findOrderByMultiFields(String orderNumber, String idleName, String minPrice, String maxPrice, String buyerNickname, String sellerNickname, Integer orderStatus, Integer paymentStatus, String startTime, String endTime, int page, int nums) {
        List<OrderModel> list = orderDao.findOrderByMultiFields(orderNumber, idleName, minPrice, maxPrice, buyerNickname, sellerNickname, orderStatus, paymentStatus, startTime, endTime, (page - 1) * nums, nums);
        attachOrderAddress(list);

        if (list.size() > 0) {
            List<Long> idleIdList = new ArrayList<>();
            List<Long> userIdList = new ArrayList<>();
            for (OrderModel i : list) {
                idleIdList.add(i.getIdleId());
                userIdList.add(i.getUserId());
            }
            List<IdleItemModel> idleItemModelList = idleItemDao.findIdleByList(idleIdList);
            List<UserModel> userModelList = userDao.findUserByList(userIdList);
            Map<Long, IdleItemModel> idleMap = new HashMap<>();
            Map<Long, UserModel> userMap = new HashMap<>();
            for (IdleItemModel idle : idleItemModelList) {
                // 同样需要加载闲置发布者的信息
                userIdList.add(idle.getUserId());
                idleMap.put(idle.getId(), idle);
            }
            // 重新查询所有相关用户（买家+卖家）
            userModelList = userDao.findUserByList(userIdList);
            for (UserModel user : userModelList) {
                userMap.put(user.getId(), user);
            }

            for (OrderModel i : list) {
                i.setIdleItem(idleMap.get(i.getIdleId()));
                if(i.getIdleItem() != null){
                     i.getIdleItem().setUser(userMap.get(i.getIdleItem().getUserId()));
                }
                i.setUser(userMap.get(i.getUserId()));
            }
        }

        int count = orderDao.countOrderByMultiFields(orderNumber, idleName, minPrice, maxPrice, buyerNickname, sellerNickname, orderStatus, paymentStatus, startTime, endTime);
        return new PageVo<>(list, count);
    }

    /**
     * 更新订单状态，无验证，后期修改为定制的更新sql
     * 后期改为在支付时下架闲置
     *
     * @param orderModel
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrder(OrderModel orderModel) {
        if (orderModel == null || orderModel.getId() == null || orderModel.getOrderStatus() == null) {
            return false;
        }
        OrderModel oldOrder = orderDao.selectByPrimaryKey(orderModel.getId());
        if (oldOrder == null || oldOrder.getOrderStatus() == null) {
            return false;
        }

        byte oldStatus = oldOrder.getOrderStatus();
        byte newStatus = orderModel.getOrderStatus();
        boolean validTransition = (oldStatus == 0 && (newStatus == 1 || newStatus == 4))
                || (oldStatus == 1 && newStatus == 2)
                || (oldStatus == 2 && newStatus == 3);
        if (!validTransition) {
            return false;
        }

        // 不可修改的信息
        orderModel.setOrderNumber(null);
        orderModel.setUserId(null);
        orderModel.setIdleId(null);
        orderModel.setOrderCount(null);
        orderModel.setCreateTime(null);
        if (newStatus == 4) {
            // 取消订单,需要优化，减少数据库查询次数
            if (orderDao.updateByPrimaryKeySelective(orderModel) == 1) {
                Integer recoverCount = oldOrder.getOrderCount() == null ? 1 : oldOrder.getOrderCount();
                if (idleItemDao.increaseStock(oldOrder.getIdleId(), recoverCount) == 1) {
                    return true;
                }
                throw new RuntimeException("回补库存失败");
            }
            throw new RuntimeException("更新订单状态失败");
        }

        if (newStatus != 1) {
            orderModel.setPaymentStatus(null);
            orderModel.setPaymentTime(null);
        }
        return orderDao.updateByPrimaryKeySelective(orderModel) == 1;
    }

    /**
     * 获取我的所有订单
     * 同时查询出对应的闲置信息，
     * 未做分页
     * userId建索引
     *
     * @param userId
     * @return
     */
    public List<OrderModel> getMyOrder(Long userId) {
        List<OrderModel> list = orderDao.getMyOrder(userId);
        if (list.size() > 0) {
            List<Long> idleIdList = new ArrayList<>();
            for (OrderModel i : list) {
                idleIdList.add(i.getIdleId());
            }
            List<IdleItemModel> idleItemModelList = idleItemDao.findIdleByList(idleIdList);
            Map<Long, IdleItemModel> map = new HashMap<>();
            for (IdleItemModel idle : idleItemModelList) {
                map.put(idle.getId(), idle);
            }
            for (OrderModel i : list) {
                i.setIdleItem(map.get(i.getIdleId()));
            }
        }
        return list;
    }

    /**
     * 查询用户卖出的闲置
     *
     * @param userId
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<OrderModel> getMySoldIdle(Long userId) {
        List<IdleItemModel> list = idleItemDao.getAllIdleItem(userId);
        List<OrderModel> orderList = null;
        if (list.size() > 0) {
            List<Long> idleIdList = new ArrayList<>();
            for (IdleItemModel i : list) {
                idleIdList.add(i.getId());
            }
            orderList = orderDao.findOrderByIdleIdList(idleIdList);
            Map<Long, IdleItemModel> map = new HashMap<>();
            for (IdleItemModel idle : list) {
                map.put(idle.getId(), idle);
            }
            for (OrderModel o : orderList) {
                o.setIdleItem(map.get(o.getIdleId()));
            }
        }
        return orderList;
    }

    @Resource
    private UserDao userDao;

    public PageVo<OrderModel> getAllOrder(int page, int nums) {
        List<OrderModel> list = orderDao.getAllOrder((page - 1) * nums, nums);
        attachOrderAddress(list);
        if (list.size() > 0) {
            List<Long> idleIdList = new ArrayList<>();
            List<Long> userIdList = new ArrayList<>();
            for (OrderModel i : list) {
                idleIdList.add(i.getIdleId());
                userIdList.add(i.getUserId());
            }
            List<IdleItemModel> idleItemModelList = idleItemDao.findIdleByList(idleIdList);
            List<UserModel> userModelList = userDao.findUserByList(userIdList);
            Map<Long, IdleItemModel> idleMap = new HashMap<>();
            Map<Long, UserModel> userMap = new HashMap<>();
            for (IdleItemModel idle : idleItemModelList) {
                // 同样需要加载闲置发布者的信息
                userIdList.add(idle.getUserId());
                idleMap.put(idle.getId(), idle);
            }
            // 重新查询所有相关用户（买家+卖家）
            userModelList = userDao.findUserByList(userIdList);
            for (UserModel user : userModelList) {
                userMap.put(user.getId(), user);
            }

            for (OrderModel i : list) {
                i.setIdleItem(idleMap.get(i.getIdleId()));
                if(i.getIdleItem() != null){
                     i.getIdleItem().setUser(userMap.get(i.getIdleItem().getUserId()));
                }
                i.setUser(userMap.get(i.getUserId()));
            }
        }
        int count = orderDao.countAllOrder();
        return new PageVo<>(list, count);
    }

    private void attachOrderAddress(List<OrderModel> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (OrderModel order : list) {
            if (order == null || order.getId() == null) {
                continue;
            }
            OrderAddressModel orderAddress = orderAddressDao.selectByOrderId(order.getId());
            order.setDetailAddress(orderAddress.getDetailAddress());
        }
    }

}
