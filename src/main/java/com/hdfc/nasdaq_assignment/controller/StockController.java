package com.hdfc.nasdaq_assignment.controller;

import com.hdfc.nasdaq_assignment.dto.StockRequest;
import com.hdfc.nasdaq_assignment.dto.StockResponse;
import com.hdfc.nasdaq_assignment.model.Stock;
import com.hdfc.nasdaq_assignment.service.StockService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/stock")
    public StockResponse getStock(@Valid @RequestBody StockRequest request, HttpServletRequest httpRequest) {
        Stock stock = stockService.getStock(request.getSymbol());
        
        long startTime = (long) httpRequest.getAttribute("startTime");
        long timeTaken = System.currentTimeMillis() - startTime;
        return new StockResponse(stock.getCompanyName(), stock.getSecurityName(), timeTaken + " ms");
    }
}
