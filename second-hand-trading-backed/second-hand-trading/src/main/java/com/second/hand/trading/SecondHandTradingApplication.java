package com.second.hand.trading;

import com.second.hand.trading.utils.OrderTaskHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.second.hand.trading.dao")
public class SecondHandTradingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondHandTradingApplication.class, args);
        OrderTaskHandler.run();
    }

}
