package com.hdfc.nasdaq_assignment.service;

import com.hdfc.nasdaq_assignment.model.Stock;
import java.util.List;

public interface StockService {
    Stock getStock(String symbol) ;
    void saveAll(List<Stock> stocks);
}
