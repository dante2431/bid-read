package com.market.bid.repository;

import com.market.bid.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findBySymbolOrderByEventTimeDesc(String symbol);

    @Query("SELECT b FROM Bid b WHERE b.symbol = :symbol AND b.eventTime >= :since ORDER BY b.eventTime DESC")
    List<Bid> findBySymbolAndEventTimeAfter(@Param("symbol") String symbol, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(b) FROM Bid b WHERE b.symbol = :symbol AND b.eventTime >= :since")
    long countBySymbolAndEventTimeAfter(@Param("symbol") String symbol, @Param("since") LocalDateTime since);

    void deleteByEventTimeBefore(LocalDateTime cutoff);
}
