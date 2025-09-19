package com.example.common.events;

public record RiskBlockedEvent(String orderId, String reason) {
}
