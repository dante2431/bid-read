package com.market.bid.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.market.bid.service.BinanceWebSocketClient;

@Component
public class ApplicationConfig {
    
    private final BinanceWebSocketClient webSocketClient;

    public ApplicationConfig(BinanceWebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("========================================");
        System.out.println("🚀 Market Data Processor Starting...");
        System.out.println("📡 Connecting to Binance WebSocket...");
        System.out.println("💾 H2 Database initialized");
        System.out.println("🔄 Multi-threaded processing enabled");
        System.out.println("========================================");
        
        webSocketClient.connect();
    }
}
