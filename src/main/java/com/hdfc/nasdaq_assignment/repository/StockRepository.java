package com.hdfc.nasdaq_assignment.repository;

import com.hdfc.nasdaq_assignment.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StockRepository extends JpaRepository<Stock, String> {

}