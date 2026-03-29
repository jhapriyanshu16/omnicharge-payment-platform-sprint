package com.omnicharge.operatorservice.service.impl;

import com.omnicharge.operatorservice.dto.request.CreateOperatorRequest;
import com.omnicharge.operatorservice.dto.response.OperatorResponse;
import com.omnicharge.operatorservice.entity.Operator;
import com.omnicharge.operatorservice.exception.OperatorAlreadyExistsException;
import com.omnicharge.operatorservice.exception.OperatorNotFoundException;
import com.omnicharge.operatorservice.repository.OperatorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperatorServiceImplTest {

    @Mock
    private OperatorRepository repository;

    @InjectMocks
    private OperatorServiceImpl operatorService;

    @Test
    void shouldCreateOperatorSuccessfully() {
        CreateOperatorRequest request = new CreateOperatorRequest();
        request.setName("Jio");
        request.setCircle("UP");

        Operator saved = Operator.builder()
                .id(1L)
                .name("Jio")
                .circle("UP")
                .build();

        when(repository.existsByNameAndCircle("Jio", "UP")).thenReturn(false);
        when(repository.save(any(Operator.class))).thenReturn(saved);

        OperatorResponse result = operatorService.create(request);

        assertEquals(saved.getId(), result.getId());
        assertEquals(saved.getName(), result.getName());
        assertEquals(saved.getCircle(), result.getCircle());
    }

    @Test
    void shouldThrowExceptionWhenOperatorAlreadyExists() {
        CreateOperatorRequest request = new CreateOperatorRequest();
        request.setName("Jio");
        request.setCircle("UP");

        when(repository.existsByNameAndCircle("Jio", "UP")).thenReturn(true);

        assertThrows(OperatorAlreadyExistsException.class, () -> operatorService.create(request));
        verify(repository, never()).save(any(Operator.class));
    }

    @Test
    void shouldGetAllOperatorsSuccessfully() {
        Operator first = Operator.builder().id(1L).name("Jio").circle("UP").build();
        Operator second = Operator.builder().id(2L).name("Airtel").circle("Delhi").build();

        when(repository.findAll()).thenReturn(List.of(first, second));

        List<OperatorResponse> result = operatorService.getAll();

        assertEquals(2, result.size());
        assertIterableEquals(List.of("Jio", "Airtel"), result.stream().map(OperatorResponse::getName).toList());
    }

    @Test
    void shouldGetOperatorByIdSuccessfully() {
        Operator operator = Operator.builder().id(1L).name("Jio").circle("UP").build();
        when(repository.findById(1L)).thenReturn(Optional.of(operator));

        OperatorResponse result = operatorService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Jio", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenOperatorNotFoundById() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OperatorNotFoundException.class, () -> operatorService.getById(99L));
    }

    @Test
    void shouldDeleteOperatorSuccessfully() {
        Operator operator = Operator.builder().id(1L).name("Jio").circle("UP").build();
        when(repository.findById(1L)).thenReturn(Optional.of(operator));

        operatorService.delete(1L);

        verify(repository).delete(operator);
    }

    @Test
    void shouldThrowExceptionWhenDeletingMissingOperator() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OperatorNotFoundException.class, () -> operatorService.delete(99L));
        verify(repository, never()).delete(any(Operator.class));
    }
}
