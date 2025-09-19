package com.example.oms.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PlaceOrderRequest(
        @NotBlank String symbol,
        @NotBlank String segment,
        @NotBlank String orderSide,
        @Min(1) int quantity,
        @NotBlank String orderType,
        Double limitPrice,
        Double triggerPrice,
        @NotBlank String timeInForce
){}

