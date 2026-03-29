package com.omnicharge.operatorservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicharge.operatorservice.dto.request.CreateOperatorRequest;
import com.omnicharge.operatorservice.dto.response.OperatorResponse;
import com.omnicharge.operatorservice.exception.GlobalExceptionHandler;
import com.omnicharge.operatorservice.exception.OperatorNotFoundException;
import com.omnicharge.operatorservice.service.OperatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OperatorController.class)
@Import(GlobalExceptionHandler.class)
class OperatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OperatorService operatorService;

    @Test
    void shouldCreateOperatorSuccessfully() throws Exception {
        CreateOperatorRequest request = new CreateOperatorRequest();
        request.setName("Jio");
        request.setCircle("Delhi");

        OperatorResponse response = OperatorResponse.builder()
                .id(1L)
                .name("Jio")
                .circle("Delhi")
                .createdAt(LocalDateTime.now())
                .build();

        when(operatorService.create(request)).thenReturn(response);

        mockMvc.perform(post("/operators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Operator created"))
                .andExpect(jsonPath("$.data.name").value("Jio"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateOperatorPayloadIsInvalid() throws Exception {
        CreateOperatorRequest request = new CreateOperatorRequest();
        request.setName("");
        request.setCircle("Delhi");

        mockMvc.perform(post("/operators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("name : Operator name is required"));
    }

    @Test
    void shouldReturnOperatorList() throws Exception {
        when(operatorService.getAll()).thenReturn(List.of(
                OperatorResponse.builder().id(1L).name("Jio").circle("Delhi").build(),
                OperatorResponse.builder().id(2L).name("Airtel").circle("UP East").build()
        ));

        mockMvc.perform(get("/operators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldReturnNotFoundWhenOperatorDoesNotExist() throws Exception {
        when(operatorService.getById(99L)).thenThrow(new OperatorNotFoundException("Operator not found"));

        mockMvc.perform(get("/operators/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Operator not found"));
    }

    @Test
    void shouldDeleteOperatorSuccessfully() throws Exception {
        doNothing().when(operatorService).delete(1L);

        mockMvc.perform(delete("/operators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Operator deleted"));
    }
}
