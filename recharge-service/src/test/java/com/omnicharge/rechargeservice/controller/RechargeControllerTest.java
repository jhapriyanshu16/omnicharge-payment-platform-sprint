package com.omnicharge.rechargeservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnicharge.rechargeservice.dto.request.RechargeRequest;
import com.omnicharge.rechargeservice.dto.response.RechargeResponse;
import com.omnicharge.rechargeservice.entity.RechargeStatus;
import com.omnicharge.rechargeservice.exception.GlobalExceptionHandler;
import com.omnicharge.rechargeservice.exception.RechargeNotFoundException;
import com.omnicharge.rechargeservice.service.RechargeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RechargeController.class)
@Import(GlobalExceptionHandler.class)
class RechargeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RechargeService rechargeService;

    @Test
    void shouldCreateRechargeSuccessfully() throws Exception {
        RechargeRequest request = new RechargeRequest();
        request.setMobileNumber("9876543210");
        request.setOperatorId(1L);
        request.setPlanId(10L);

        RechargeResponse response = RechargeResponse.builder()
                .rechargeId(1L)
                .mobileNumber("9876543210")
                .operatorId(1L)
                .planId(10L)
                .amount(299.0)
                .status(RechargeStatus.SUCCESS)
                .build();

        when(rechargeService.createRecharge("user@example.com", request)).thenReturn(response);

        mockMvc.perform(post("/recharges")
                        .header("X-User-Email", "user@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Recharge successful"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void shouldReturnBadRequestWhenRechargePayloadIsInvalid() throws Exception {
        RechargeRequest request = new RechargeRequest();
        request.setMobileNumber("123");
        request.setOperatorId(0L);
        request.setPlanId(null);

        mockMvc.perform(post("/recharges")
                        .header("X-User-Email", "user@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnRechargeDetails() throws Exception {
        RechargeResponse response = RechargeResponse.builder()
                .rechargeId(1L)
                .mobileNumber("9876543210")
                .operatorId(1L)
                .planId(10L)
                .amount(299.0)
                .status(RechargeStatus.PENDING)
                .build();

        when(rechargeService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/recharges/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Recharge details"))
                .andExpect(jsonPath("$.data.rechargeId").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenRechargeDoesNotExist() throws Exception {
        when(rechargeService.getById(99L)).thenThrow(new RechargeNotFoundException("Recharge not found"));

        mockMvc.perform(get("/recharges/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Recharge not found"));
    }

    @Test
    void shouldUpdateRechargeStatusSuccessfully() throws Exception {
        doNothing().when(rechargeService).updateStatus(1L, "SUCCESS");

        mockMvc.perform(put("/recharges/1/status")
                        .param("status", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
