package com.omnicharge.operatorservice.repository;

import com.omnicharge.operatorservice.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    boolean existsByOperatorIdAndPrice(Long operatorId, Double price);

    List<Plan> findByOperatorId(Long operatorId);
}
