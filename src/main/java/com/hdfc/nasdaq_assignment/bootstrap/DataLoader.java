package com.hdfc.nasdaq_assignment.bootstrap;

import com.hdfc.nasdaq_assignment.model.Stock;
import com.hdfc.nasdaq_assignment.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final StockService stockService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting CSV data loading process...");
        List<Stock> stocks = new ArrayList<>();

        try (Reader reader = new InputStreamReader(new ClassPathResource("static/nasdaq-listed-symbols.csv").getInputStream())) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            for (CSVRecord record : records) {
                String symbol = record.get("Symbol");
                String companyName = record.get("Company Name");
                String securityName = record.get("Security Name");

                Stock stock = new Stock(symbol, companyName, securityName);
                stocks.add(stock);
            }

            stockService.saveAll(stocks);
            log.info("Successfully loaded {} stocks from CSV", stocks.size());
        } catch (Exception e) {
            log.error("Failed to load CSV data: {}", e.getMessage(), e);
        }
    }
}
