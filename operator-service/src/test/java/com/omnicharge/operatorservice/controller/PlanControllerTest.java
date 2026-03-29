package com.omnicharge.operatorservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicharge.operatorservice.dto.request.CreatePlanRequest;
import com.omnicharge.operatorservice.dto.response.PlanResponse;
import com.omnicharge.operatorservice.exception.GlobalExceptionHandler;
import com.omnicharge.operatorservice.exception.PlanNotFoundException;
import com.omnicharge.operatorservice.service.PlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanController.class)
@Import(GlobalExceptionHandler.class)
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlanService planService;

    @Test
    void shouldCreatePlanSuccessfully() throws Exception {
        CreatePlanRequest request = new CreatePlanRequest();
        request.setOperatorId(1L);
        request.setPrice(299.0);
        request.setValidity(28);
        request.setData(2.0);
        request.setTalktime(100.0);

        PlanResponse response = PlanResponse.builder()
                .id(1L)
                .operatorId(1L)
                .operatorName("Jio")
                .price(299.0)
                .validity(28)
                .data(2.0)
                .talktime(100.0)
                .build();

        when(planService.create(request)).thenReturn(response);

        mockMvc.perform(post("/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Plan created"))
                .andExpect(jsonPath("$.data.operatorName").value("Jio"));
    }

    @Test
    void shouldReturnBadRequestWhenPlanPayloadIsInvalid() throws Exception {
        CreatePlanRequest request = new CreatePlanRequest();
        request.setOperatorId(null);
        request.setPrice(-10.0);
        request.setValidity(0);
        request.setData(-1.0);
        request.setTalktime(-5.0);

        mockMvc.perform(post("/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnPlansForOperator() throws Exception {
        when(planService.getPlansByOperator(1L)).thenReturn(List.of(
                PlanResponse.builder().id(1L).operatorId(1L).price(199.0).build(),
                PlanResponse.builder().id(2L).operatorId(1L).price(299.0).build()
        ));

        mockMvc.perform(get("/plans/operator/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldReturnNotFoundWhenPlanDoesNotExist() throws Exception {
        when(planService.getById(99L)).thenThrow(new PlanNotFoundException("Plan not found"));

        mockMvc.perform(get("/plans/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Plan not found"));
    }

    @Test
    void shouldDeletePlanSuccessfully() throws Exception {
        doNothing().when(planService).delete(1L);

        mockMvc.perform(delete("/plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Plan deleted"));
    }
}
