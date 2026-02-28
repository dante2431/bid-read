package com.market.bid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BidApplication {
    public static void main(String[] args) {
        SpringApplication.run(BidApplication.class, args);
    }
}
