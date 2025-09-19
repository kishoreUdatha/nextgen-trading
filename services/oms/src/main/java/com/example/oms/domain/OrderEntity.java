package com.example.oms.domain;

import com.example.oms.enums.OrderStatus;
import com.example.oms.enums.OrderType;
import com.example.oms.enums.Side;
import com.example.oms.enums.TimeInForce;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

import lombok.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class OrderEntity {

    @Id
    private UUID id;

    private String clientOrderId;
    private String userId;
    private String symbol;
    private String segment;

    @Enumerated(EnumType.STRING)
    private Side side;

    private int qty;

    private Double price;
    private Double triggerPrice;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    private TimeInForce tif;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Instant createdAt;
    private Instant updatedAt;
}
