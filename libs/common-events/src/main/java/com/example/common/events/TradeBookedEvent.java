package com.example.common.events;

public record TradeBookedEvent(String orderId, String execId, int qty, double price) {
}
