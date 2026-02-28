package com.market.bid.service;

import com.market.bid.dto.BidEvent;
import com.market.bid.entity.Bid;
import com.market.bid.repository.BidRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EventProcessor.class);
    private static final int THREAD_POOL_SIZE = 4;
    private static final int QUEUE_CAPACITY = 10000;

    private final ExecutorService executorService;
    private final BlockingQueue<BidEvent> eventQueue;
    private final BidRepository bidRepository;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Autowired
    public EventProcessor(BidRepository bidRepository) {
        this.bidRepository = bidRepository;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.eventQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        
        startWorkers();
    }

    public void processEvent(BidEvent bidEvent) {
        try {
            boolean offered = eventQueue.offer(bidEvent);
            if (!offered) {
                logger.warn("Event queue is full, dropping event: {}", bidEvent);
            }
        } catch (Exception e) {
            logger.error("Error queuing event", e);
        }
    }

    private void startWorkers() {
        isRunning.set(true);
        
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            final int workerId = i;
            executorService.submit(() -> {
                logger.info("Starting worker thread {}", workerId);
                
                while (isRunning.get() || !eventQueue.isEmpty()) {
                    try {
                        BidEvent event = eventQueue.take();
                        processBidEvent(event);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.info("Worker thread {} interrupted", workerId);
                        break;
                    } catch (Exception e) {
                        logger.error("Error in worker thread {}", workerId, e);
                    }
                }
                
                logger.info("Worker thread {} stopped", workerId);
            });
        }
    }

    private void processBidEvent(BidEvent bidEvent) {
        try {
            Bid bid = new Bid(
                bidEvent.getSymbol(),
                bidEvent.getPrice(),
                bidEvent.getQuantity(),
                bidEvent.getEventTime()
            );
            
            bidRepository.save(bid);
            
            logger.debug("Processed and saved bid: {}", bid);
        } catch (Exception e) {
            logger.error("Error processing bid event: {}", bidEvent, e);
        }
    }

    public void shutdown() {
        logger.info("Shutting down event processor...");
        isRunning.set(false);
        
        executorService.shutdown();
        
        try {
            if (!executorService.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Event processor shutdown complete");
    }

    public int getQueueSize() {
        return eventQueue.size();
    }

    public boolean isQueueFull() {
        return eventQueue.remainingCapacity() == 0;
    }
}
