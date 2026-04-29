package com.second.hand.trading.controller;

import com.second.hand.trading.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/address")
public class AdminAddressController {

    @Autowired
    private AddressService addressService;

}
