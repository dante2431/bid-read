package com.market.bid.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidEvent {
    private final String symbol;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final LocalDateTime eventTime;

    public BidEvent(String symbol, BigDecimal price, BigDecimal quantity, LocalDateTime eventTime) {
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.eventTime = eventTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    @Override
    public String toString() {
        return "BidEvent{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", eventTime=" + eventTime +
                '}';
    }
}
