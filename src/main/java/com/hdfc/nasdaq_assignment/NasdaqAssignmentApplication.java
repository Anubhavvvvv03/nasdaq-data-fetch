package com.hdfc.nasdaq_assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class NasdaqAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(NasdaqAssignmentApplication.class, args);
	}

}
