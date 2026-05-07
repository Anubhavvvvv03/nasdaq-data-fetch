package com.hdfc.nasdaq_assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockRequest {
    @NotBlank(message = "Symbol must not be blank")
    @Size(min = 1, max = 10, message = "Symbol length must be between 1 and 10 characters")
    private String symbol;
}
