package com.omnicharge.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "RECHARGE-SERVICE")
public interface RechargeClient {

    @PutMapping("/recharges/{id}/status")
    void updateStatus(@PathVariable("id") Long id,
                      @RequestParam("status") String status);
}