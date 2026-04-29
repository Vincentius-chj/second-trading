package com.second.hand.trading.service;

import com.second.hand.trading.model.AdminModel;
import com.second.hand.trading.vo.PageVo;

public interface AdminService {

    AdminModel login(String accountNumber, String adminPassword);

    PageVo<AdminModel> getAdminList(int page, int nums);

    boolean addAdmin(AdminModel adminModel);

}
