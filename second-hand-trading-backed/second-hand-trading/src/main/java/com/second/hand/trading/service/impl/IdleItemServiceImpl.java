package com.second.hand.trading.service.impl;

import com.second.hand.trading.dao.IdleItemDao;
import com.second.hand.trading.dao.UserDao;
import com.second.hand.trading.model.IdleItemModel;
import com.second.hand.trading.model.UserModel;
import com.second.hand.trading.service.IdleItemService;
import com.second.hand.trading.vo.PageVo;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IdleItemServiceImpl implements IdleItemService {

    @Resource
    private IdleItemDao idleItemDao;

    @Resource
    private UserDao userDao;

    /**
     * 发布闲置
     *
     * @param idleItemModel
     * @return
     */
    public boolean addIdleItem(IdleItemModel idleItemModel) {
        return idleItemDao.insert(idleItemModel) == 1;
    }

    private void normalizeIdleStock(IdleItemModel idleItemModel) {
        if (idleItemModel != null && idleItemModel.getIdleStock() == null) {
            idleItemModel.setIdleStock(1);
        }
    }

    private void normalizeIdleStock(List<IdleItemModel> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (IdleItemModel item : list) {
            normalizeIdleStock(item);
        }
    }

    /**
     * 查询闲置信息，同时查出发布者的信息
     *
     * @param id
     * @return
     */
    public IdleItemModel getIdleItem(Long id) {
        IdleItemModel idleItemModel = idleItemDao.selectByPrimaryKey(id);
        normalizeIdleStock(idleItemModel);
        if (idleItemModel != null) {
            idleItemModel.setUser(userDao.selectByPrimaryKey(idleItemModel.getUserId()));
        }
        return idleItemModel;
    }

    /**
     * 查询用户发布的所有闲置
     * user_id建索引
     *
     * @param userId
     * @return
     */
    public List<IdleItemModel> getAllIdelItem(Long userId) {
        List<IdleItemModel> list = idleItemDao.getAllIdleItem(userId);
        normalizeIdleStock(list);
        return list;
    }

    /**
     * 搜索，分页
     * 同时查出闲置发布者的信息
     *
     * @param findValue
     * @param page
     * @param nums
     * @return
     */
    public PageVo<IdleItemModel> findIdleItem(String findValue, int page, int nums) {
        List<IdleItemModel> list = idleItemDao.findIdleItem(findValue, (page - 1) * nums, nums);
        normalizeIdleStock(list);

        for (IdleItemModel i : list) {
            System.out.println(i.getIdleName() + ' ' + i.getIdleStatus() + '\n');
        }

        if (list.size() > 0) {
            List<Long> idList = new ArrayList<>();
            for (IdleItemModel i : list) {
                idList.add(i.getUserId());
            }
            List<UserModel> userList = userDao.findUserByList(idList);
            Map<Long, UserModel> map = new HashMap<>();
            for (UserModel user : userList) {
                map.put(user.getId(), user);
            }
            for (IdleItemModel i : list) {
                i.setUser(map.get(i.getUserId()));
            }
        }

        int count = idleItemDao.countIdleItem(findValue);
        return new PageVo<>(list, count);
    }


    @Override
    public PageVo<IdleItemModel> findIdleItemByMultiFields(String idleName, String minPrice, String maxPrice, Integer idleLabel, String idlePlace, String userNickname, String startTime, String endTime, int page, int nums) {
        List<IdleItemModel> list = idleItemDao.findIdleItemByMultiFields(idleName, minPrice, maxPrice, idleLabel, idlePlace, userNickname, startTime, endTime, (page - 1) * nums, nums);
        normalizeIdleStock(list);

        if (list.size() > 0) {
            List<Long> idList = new ArrayList<>();
            for (IdleItemModel i : list) {
                idList.add(i.getUserId());
            }
            List<UserModel> userList = userDao.findUserByList(idList);
            Map<Long, UserModel> map = new HashMap<>();
            for (UserModel user : userList) {
                map.put(user.getId(), user);
            }
            for (IdleItemModel i : list) {
                i.setUser(map.get(i.getUserId()));
            }
        }

        int count = idleItemDao.countIdleItemByMultiFields(idleName, minPrice, maxPrice, idleLabel, idlePlace, userNickname, startTime, endTime);
        return new PageVo<>(list, count);
    }


    @Override
    public PageVo<IdleItemModel> findIdleItemByMultiFieldsWithStatus(String idleName, String minPrice, String maxPrice, Integer idleLabel, String idlePlace, String userNickname, String startTime, String endTime, Integer status, int page, int nums) {
        List<IdleItemModel> list = idleItemDao.findIdleItemByMultiFieldsWithStatus(idleName, minPrice, maxPrice, idleLabel, idlePlace, userNickname, startTime, endTime, status, (page - 1) * nums, nums);
        normalizeIdleStock(list);

        if (list.size() > 0) {
            List<Long> idList = new ArrayList<>();
            for (IdleItemModel i : list) {
                idList.add(i.getUserId());
            }
            List<UserModel> userList = userDao.findUserByList(idList);
            Map<Long, UserModel> map = new HashMap<>();
            for (UserModel user : userList) {
                map.put(user.getId(), user);
            }
            for (IdleItemModel i : list) {
                i.setUser(map.get(i.getUserId()));
            }
        }

        int count = idleItemDao.countIdleItemByMultiFieldsWithStatus(idleName, minPrice, maxPrice, idleLabel, idlePlace, userNickname, startTime, endTime, status);
        return new PageVo<>(list, count);
    }


    /**
     * 分类查询，分页
     * 同时查出闲置发布者的信息，代码结构与上面的类似，可封装优化，或改为join查询
     *
     * @param idleLabel
     * @param page
     * @param nums
     * @return
     */
    public PageVo<IdleItemModel> findIdleItemByLable(int idleLabel, int page, int nums) {
        List<IdleItemModel> list = idleItemDao.findIdleItemByLable(idleLabel, (page - 1) * nums, nums);
        normalizeIdleStock(list);
        if (list.size() > 0) {
            List<Long> idList = new ArrayList<>();
            for (IdleItemModel i : list) {
                idList.add(i.getUserId());
            }
            List<UserModel> userList = userDao.findUserByList(idList);
            Map<Long, UserModel> map = new HashMap<>();
            for (UserModel user : userList) {
                map.put(user.getId(), user);
            }
            for (IdleItemModel i : list) {
                i.setUser(map.get(i.getUserId()));
            }
        }
        int count = idleItemDao.countIdleItemByLable(idleLabel);
        return new PageVo<>(list, count);
    }

    /**
     * 更新闲置信息
     *
     * @param idleItemModel
     * @return
     */
    public boolean updateIdleItem(IdleItemModel idleItemModel) {
        return idleItemDao.updateByPrimaryKeySelective(idleItemModel) == 1;
    }

    public PageVo<IdleItemModel> adminGetIdleList(int status, int page, int nums) {
        List<IdleItemModel> list = idleItemDao.getIdleItemByStatus(status, (page - 1) * nums, nums);
        normalizeIdleStock(list);
        if (list.size() > 0) {
            List<Long> idList = new ArrayList<>();
            for (IdleItemModel i : list) {
                idList.add(i.getUserId());
            }
            List<UserModel> userList = userDao.findUserByList(idList);
            Map<Long, UserModel> map = new HashMap<>();
            for (UserModel user : userList) {
                map.put(user.getId(), user);
            }
            for (IdleItemModel i : list) {
                i.setUser(map.get(i.getUserId()));
            }
        }
        int count = idleItemDao.countIdleItemByStatus(status);
        return new PageVo<>(list, count);
    }


    // 根据不同的状态查找闲置物品（已修改为支持任意状态）
    @Override
    public PageVo<IdleItemModel> findIdleItemByStatus(String findValue, int status, int page, int nums) {

        List<IdleItemModel> list = idleItemDao.findIdleItemByStatus(findValue, status, (page - 1) * nums, nums);
        normalizeIdleStock(list);

        if (list.size() > 0) {
            List<Long> idList = new ArrayList<>();
            for (IdleItemModel i : list) {
                idList.add(i.getUserId());
            }
            List<UserModel> userList = userDao.findUserByList(idList);
            Map<Long, UserModel> map = new HashMap<>();
            for (UserModel user : userList) {
                map.put(user.getId(), user);
            }
            for (IdleItemModel i : list) {
                i.setUser(map.get(i.getUserId()));
            }
        }
        int count = idleItemDao.countIdleItemByStatusAndFindValue(findValue, status);

        return new PageVo<>(list, count);

    }
    
    // 根据不同的状态查找闲置物品（原版本，兼容旧调用）
    @Override
    public PageVo<IdleItemModel> findIdleItem1(String findValue, int status, int page, int nums) {

        List<IdleItemModel> list = idleItemDao.findIdleItem1(findValue, status, (page - 1) * nums, nums);
        normalizeIdleStock(list);

        if (list.size() > 0) {
            List<Long> idList = new ArrayList<>();
            for (IdleItemModel i : list) {
                idList.add(i.getUserId());
            }
            List<UserModel> userList = userDao.findUserByList(idList);
            Map<Long, UserModel> map = new HashMap<>();
            for (UserModel user : userList) {
                map.put(user.getId(), user);
            }
            for (IdleItemModel i : list) {
                i.setUser(map.get(i.getUserId()));
            }
        }
        int count = idleItemDao.countIdleItem(findValue);

        return new PageVo<>(list, count);

    }
}
