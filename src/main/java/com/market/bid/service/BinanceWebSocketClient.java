package com.market.bid.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.bid.dto.BidEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.websocket.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@ClientEndpoint
public class BinanceWebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(BinanceWebSocketClient.class);
    private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/btcusdt@trade";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final EventProcessor eventProcessor;
    private Session session;
    private final CountDownLatch latch = new CountDownLatch(1);

    public BinanceWebSocketClient(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(BINANCE_WS_URL));
            latch.await();
            logger.info("WebSocket connection established");
        } catch (Exception e) {
            logger.error("Failed to connect to Binance WebSocket", e);
            throw new RuntimeException("WebSocket connection failed", e);
        }
    }

    public void disconnect() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
            connected.set(false);
            logger.info("WebSocket connection closed");
        } catch (IOException e) {
            logger.error("Error closing WebSocket connection", e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        connected.set(true);
        latch.countDown();
        logger.info("Connected to Binance WebSocket");
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            
            if (jsonNode.has("e") && "trade".equals(jsonNode.get("e").asText())) {
                BidEvent bidEvent = new BidEvent(
                    jsonNode.get("s").asText(),
                    new BigDecimal(jsonNode.get("p").asText()),
                    new BigDecimal(jsonNode.get("q").asText()),
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(jsonNode.get("T").asLong()),
                        ZoneId.systemDefault()
                    )
                );
                
                eventProcessor.processEvent(bidEvent);
            }
        } catch (Exception e) {
            logger.error("Error processing WebSocket message: {}", message, e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        connected.set(false);
        logger.info("WebSocket connection closed: {}", closeReason);
        
        if (!closeReason.getCloseCode().equals(CloseReason.CloseCodes.NORMAL_CLOSURE)) {
            logger.info("Attempting to reconnect...");
            reconnect();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("WebSocket error occurred", throwable);
        connected.set(false);
    }

    public boolean isConnected() {
        return connected.get();
    }

    private void reconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                if (!connected.get()) {
                    connect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
