package com.omnicharge.operatorservice.repository;

import com.omnicharge.operatorservice.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatorRepository extends JpaRepository<Operator, Long> {

    boolean existsByNameAndCircle(String name, String circle);
}