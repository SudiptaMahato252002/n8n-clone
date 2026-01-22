package com.example.n8n.services.Outbox;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.n8n.models.dtos.outbox.ExecutionEventPayload;
import com.example.n8n.models.entity.OutboxEvent;
import com.example.n8n.repo.OutboxRepo;
import com.example.n8n.services.Execution.ExecutionQueueService;
import com.example.n8n.utils.JsonUtils;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxProcessor 
{
    private static final  Integer maxRetries=5;
    private static final Boolean outboxEnabled=true;
    
    private final OutboxRepo outboxRepo;    
    private final OutboxService outboxService;
    private final ExecutionQueueService executionQueueService;
    private final JsonUtils jsonUtils;

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents()
    {
        if (!outboxEnabled) 
        {
            return;
        }

        try 
        {
            List<OutboxEvent> events=outboxRepo.findUnprocessedEventsWithRetryLimit(maxRetries);
            if (events.isEmpty()) {
                return;
            }
            
            log.info("Processing {} outbox events", events.size());
            for(OutboxEvent event:events)
            {
                processEvents(event);
            }
               
        } 
        catch (Exception e) 
        {
            log.error("Error processing outbox events", e);
        }
    }

    private void processEvents(OutboxEvent event)
    {
        try 
        {
            log.debug("Processing outbox event: id={}, executionId={}", 
                event.getId(), event.getAggregateId());
            ExecutionEventPayload payload=jsonUtils.jsonToObject(event.getPayload(), ExecutionEventPayload.class);
            executionQueueService.publishToRedis(payload);
            outboxService.markAsProcessed(event.getId());
            log.info("Outbox event processed successfully: id={}, executionId={}", 
                event.getId(), event.getAggregateId());
        } 
        catch (Exception e) 
        {
            log.error("Error processing outbox events", e);
            outboxService.incremetnRetryCount(event.getId(), e.getMessage());
            
            if (event.getRetryCount() + 1 >= maxRetries) {
                log.error("Outbox event exceeded max retries and will be skipped: id={}, executionId={}", 
                    event.getId(), event.getAggregateId());
            }

        }
    }

    public OutboxStatistics getStatistics()
    {
        long unprocessedCount=outboxRepo.countByProcessedFalse();
        long totalCount=outboxRepo.count();
        long processedCount=totalCount-unprocessedCount;
        return OutboxStatistics.builder().unprocessedCount(unprocessedCount).processedCount(processedCount).totalCount(totalCount).build();

    }
    
    @Data
    @Builder
    public static class OutboxStatistics {
        private long unprocessedCount;
        private long processedCount;
        private long totalCount;
    }
}
