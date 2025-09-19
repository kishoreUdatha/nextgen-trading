package com.example.common.events;

public record OrderPlacedEvent(String orderId, String userId, String symbol, int qty) {
}
