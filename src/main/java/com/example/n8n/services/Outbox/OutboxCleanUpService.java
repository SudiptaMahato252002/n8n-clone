package com.example.n8n.services.Outbox;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.n8n.repo.OutboxRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxCleanUpService 
{
    private final OutboxRepo outboxRepo;
    private Integer retentionDays=7;
    private static final boolean cleanupEnabled=true;
    
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void CleanUpOldEvents()
    {   
        if (!cleanupEnabled) {
            return;
        }

        try 
        {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            log.info("Starting outbox cleanup for events processed before: {}", cutoffDate);
            int deletedCount=outboxRepo.deleteOldProcessedEvents(cutoffDate);
            log.info("Outbox cleanup completed. Deleted {} old events", deletedCount);

            
        } 
        catch (Exception e) 
        {
            log.error("Error during outbox cleanup", e);
        }
    }

    @Transactional
    public int cleanupManually(int daysOld) 
    {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        log.info("Manual cleanup triggered for events before: {}", cutoffDate);
        int deletedCount = outboxRepo.deleteOldProcessedEvents(cutoffDate);
        log.info("Manual cleanup completed. Deleted {} events", deletedCount);
        
        return deletedCount;
    }
    
}
