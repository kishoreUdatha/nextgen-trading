package com.example.oms.response;

import java.time.Instant;

public record OrderResponse(String orderId, String orderStatus, Instant createdAt){}
