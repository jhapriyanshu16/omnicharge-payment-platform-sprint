package com.omnicharge.operatorservice.service.impl;

import com.omnicharge.operatorservice.dto.request.CreateOperatorRequest;
import com.omnicharge.operatorservice.dto.response.OperatorResponse;
import com.omnicharge.operatorservice.entity.Operator;
import com.omnicharge.operatorservice.exception.OperatorAlreadyExistsException;
import com.omnicharge.operatorservice.exception.OperatorNotFoundException;
import com.omnicharge.operatorservice.repository.OperatorRepository;
import com.omnicharge.operatorservice.service.OperatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OperatorServiceImpl implements OperatorService {

    private final OperatorRepository repository;

    @Override
    public OperatorResponse create(CreateOperatorRequest request) {

        log.info("Creating operator {} {}", request.getName(), request.getCircle());

        if(repository.existsByNameAndCircle(request.getName(), request.getCircle())){
            throw new OperatorAlreadyExistsException("Operator already exists for this circle");
        }

        Operator op = Operator.builder()
                .name(request.getName())
                .circle(request.getCircle())
                .build();

        Operator saved = repository.save(op);

        return map(saved);
    }

    @Override
    public List<OperatorResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public OperatorResponse getById(Long id) {

        Operator op = repository.findById(id)
                .orElseThrow(() -> new OperatorNotFoundException("Operator not found"));

        return map(op);
    }

    @Override
    public void delete(Long id) {

        Operator op = repository.findById(id)
                .orElseThrow(() -> new OperatorNotFoundException("Operator not found"));

        repository.delete(op);
    }

    private OperatorResponse map(Operator op){

        return OperatorResponse.builder()
                .id(op.getId())
                .name(op.getName())
                .circle(op.getCircle())
                .createdAt(op.getCreatedAt())
                .build();
    }
}