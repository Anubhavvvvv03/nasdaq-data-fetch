package com.hdfc.nasdaq_assignment.service.impl;

import com.hdfc.nasdaq_assignment.exception.ResourceNotFoundException;
import com.hdfc.nasdaq_assignment.model.Stock;
import com.hdfc.nasdaq_assignment.repository.StockRepository;
import com.hdfc.nasdaq_assignment.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Override
    @Cacheable(value = "stocks", key = "#symbol")
    public Stock getStock(String symbol) {
        log.info("Fetching stock with symbol: {}", symbol);

        return stockRepository.findById(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock with symbol " + symbol + " not found"));
    }

    @Override
    public void saveAll(List<Stock> stocks) {
        log.info("Saving {} stocks to the database", stocks.size());
        stockRepository.saveAll(stocks);
    }
}
