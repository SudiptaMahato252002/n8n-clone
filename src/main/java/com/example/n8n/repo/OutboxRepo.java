package com.example.n8n.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.n8n.models.entity.OutboxEvent;

@Repository
public interface OutboxRepo extends JpaRepository<OutboxEvent,String> 
{
    List<OutboxEvent> findByProcessedFalseOrderByCreatedAtAsc();

    @Query("""
            SELECT o FROM OutboxEvent o
            WHERE o.processed = false
                AND o.retryCount < :maxRetries
            ORDER BY o.createdAt ASC
            """)
    List<OutboxEvent> findUnprocessedEventsWithRetryLimit(@Param("maxRetries") int maxRetries);
    
    @Modifying
    @Query("""
            DELETE o FROM OutboxEvent o
            WHERE o.processed=true
                AND O.processedAt < :beforeDate
            """)
    int deleteOldProcessedEvents(@Param("beforeDate")LocalDateTime beforeDate);
    
    long countByProcessedFalse();

}
