package com.hdfc.nasdaq_assignment;

import com.hdfc.nasdaq_assignment.controller.StockController;
import com.hdfc.nasdaq_assignment.dto.StockRequest;
import com.hdfc.nasdaq_assignment.exception.GlobalExceptionHandler;
import com.hdfc.nasdaq_assignment.exception.ResourceNotFoundException;
import com.hdfc.nasdaq_assignment.filter.TimeTrackingFilter;
import com.hdfc.nasdaq_assignment.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StockController.class)
@Import({GlobalExceptionHandler.class, TimeTrackingFilter.class})
public class ExceptionHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CacheManager cacheManager;

    @MockitoBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockitoBean
    private StockService stockService;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void whenStockNotFound_shouldReturn404ProblemDetail() throws Exception {
        when(stockService.getStock(anyString())).thenThrow(new ResourceNotFoundException("Stock not found"));

        StockRequest request = new StockRequest("INVALID");

        mockMvc.perform(post("/api/v1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("ResourceNotFoundException"))
                .andExpect(jsonPath("$.errorCode").value("STOCK_NOT_FOUND"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenValidationFails_shouldReturn400ProblemDetail() throws Exception {
        StockRequest request = new StockRequest(""); // Blank symbol

        mockMvc.perform(post("/api/v1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("symbol"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void whenGenericErrorOccurs_shouldReturn500ProblemDetail() throws Exception {
        when(stockService.getStock(anyString())).thenThrow(new RuntimeException("Unexpected error"));

        StockRequest request = new StockRequest("AAPL");

        mockMvc.perform(post("/api/v1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.traceId").exists());
    }
}
