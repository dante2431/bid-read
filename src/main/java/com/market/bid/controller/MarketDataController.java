package com.market.bid.controller;

import com.market.bid.entity.Bid;
import com.market.bid.repository.BidRepository;
import com.market.bid.service.EventProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "*")
public class MarketDataController {

    private final BidRepository bidRepository;
    private final EventProcessor eventProcessor;

    @Autowired
    public MarketDataController(BidRepository bidRepository, EventProcessor eventProcessor) {
        this.bidRepository = bidRepository;
        this.eventProcessor = eventProcessor;
    }

    @GetMapping("/bids/{symbol}")
    public ResponseEntity<List<Bid>> getRecentBids(@PathVariable String symbol) {
        List<Bid> bids = bidRepository.findBySymbolOrderByEventTimeDesc(symbol);
        return ResponseEntity.ok(bids);
    }

    @GetMapping("/bids/{symbol}/since")
    public ResponseEntity<List<Bid>> getBidsSince(@PathVariable String symbol, 
                                                  @RequestParam String since) {
        LocalDateTime sinceTime = LocalDateTime.parse(since);
        List<Bid> bids = bidRepository.findBySymbolAndEventTimeAfter(symbol, sinceTime);
        return ResponseEntity.ok(bids);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long btcTrades = bidRepository.countBySymbolAndEventTimeAfter("BTCUSDT", oneHourAgo);
        
        stats.put("queueSize", eventProcessor.getQueueSize());
        stats.put("isQueueFull", eventProcessor.isQueueFull());
        stats.put("btcTradesLastHour", btcTrades);
        stats.put("totalBids", bidRepository.count());
        
        return ResponseEntity.ok(stats);
    }
}
