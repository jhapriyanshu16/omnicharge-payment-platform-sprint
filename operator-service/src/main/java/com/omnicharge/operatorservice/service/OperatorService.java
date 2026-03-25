package com.omnicharge.operatorservice.service;

import com.omnicharge.operatorservice.dto.request.CreateOperatorRequest;
import com.omnicharge.operatorservice.dto.response.OperatorResponse;

import java.util.List;

public interface OperatorService {

    OperatorResponse create(CreateOperatorRequest request);

    List<OperatorResponse> getAll();

    OperatorResponse getById(Long id);

    void delete(Long id);
}