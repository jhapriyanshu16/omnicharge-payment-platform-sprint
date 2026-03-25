package com.omnicharge.operatorservice.controller;

import com.omnicharge.operatorservice.dto.request.CreateOperatorRequest;
import com.omnicharge.operatorservice.dto.response.ApiResponse;
import com.omnicharge.operatorservice.dto.response.OperatorResponse;
import com.omnicharge.operatorservice.service.OperatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operators")
@RequiredArgsConstructor
public class OperatorController {

    private final OperatorService service;

    @PostMapping
    public ApiResponse<OperatorResponse> create(@Valid @RequestBody CreateOperatorRequest request){

        return ApiResponse.<OperatorResponse>builder()
                .success(true)
                .message("Operator created")
                .data(service.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<OperatorResponse>> getAll(){

        return ApiResponse.<List<OperatorResponse>>builder()
                .success(true)
                .message("Operator list")
                .data(service.getAll())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OperatorResponse> getById(@PathVariable Long id){

        return ApiResponse.<OperatorResponse>builder()
                .success(true)
                .message("Operator found")
                .data(service.getById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id){

        service.delete(id);

        return ApiResponse.builder()
                .success(true)
                .message("Operator deleted")
                .build();
    }
}