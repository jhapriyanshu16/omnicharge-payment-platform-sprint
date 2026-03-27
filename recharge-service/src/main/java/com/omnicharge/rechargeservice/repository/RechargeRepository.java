package com.omnicharge.rechargeservice.repository;

import com.omnicharge.rechargeservice.entity.Recharge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RechargeRepository extends JpaRepository<Recharge, Long> {
}