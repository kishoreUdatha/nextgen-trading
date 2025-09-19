package com.example.common.events;

public record ExecReportEvent(String orderId, String execId, int qty, double price, String status) {
}
