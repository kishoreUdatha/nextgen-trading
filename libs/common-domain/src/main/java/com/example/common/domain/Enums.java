package com.example.common.domain;

public class Enums {
    public enum Side {BUY, SELL}

    public enum OrderType {MARKET, LIMIT, SL, SL_M}

    public enum TIF {DAY, IOC, GTC}

    public enum Segment {CASH, INDEX_FUT, INDEX_OPT, STOCK_FUT, STOCK_OPT}

    public enum OrderStatus {NEW, VALIDATED, REJECTED, ROUTED, PARTIALLY_FILLED, FILLED, CANCELLED, EXPIRED}
}
