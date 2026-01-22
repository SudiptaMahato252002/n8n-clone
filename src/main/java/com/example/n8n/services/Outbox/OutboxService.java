package com.example.n8n.services.Outbox;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.n8n.models.dtos.outbox.ExecutionEventPayload;
import com.example.n8n.models.entity.OutboxEvent;
import com.example.n8n.repo.OutboxRepo;
import com.example.n8n.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService 
{
    private final OutboxRepo outboxRepo;
    private final JsonUtils jsonUtils;
    private static final String AGGREGATE_TYPE_EXECUTION = "WORKFLOW_EXECUTION";
    private static final String EVENT_TYPE_EXECUTION_CREATED = "EXECUTION_CREATED";

    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent saveExecutionEvent(String executionId,String workflowId,Map<String,Object> triggerPayload)
    {
        ExecutionEventPayload payload=ExecutionEventPayload.builder()
                                        .executionId(executionId)
                                        .workflowId(workflowId)
                                        .triggerPayload(triggerPayload)
                                        .build();
        OutboxEvent event=OutboxEvent.builder()
            .aggregateType(AGGREGATE_TYPE_EXECUTION)
            .aggregateId(executionId)
            .eventType(EVENT_TYPE_EXECUTION_CREATED)
            .processed(false)
            .payload(jsonUtils.ObjectToJson(payload))
        .build();

        OutboxEvent saved=outboxRepo.save(event);
        log.info("Outbox event saved: eventId={}, executionId={}", saved.getId(), executionId);
        return saved;
    }

    @Transactional
    public void markAsProcessed(String eventId)
    {   
        log.debug("Marking outbox event as processed: {}", eventId);
        outboxRepo.findById(eventId).ifPresent(event->{
            event.setProcessed(true);
            event.setProcessedAt(LocalDateTime.now());
            outboxRepo.save(event);
        });

    }

    @Transactional
    public void incremetnRetryCount(String eventId,String errorMessage)
    {
        log.warn("Incrementing retry count for event: {}, error: {}", eventId, errorMessage);
        outboxRepo.findById(eventId).ifPresent(event->{
            event.setRetryCount(event.getRetryCount()+1);
            event.setErrorMessage(errorMessage);
            outboxRepo.save(event);
        });
    }


}
