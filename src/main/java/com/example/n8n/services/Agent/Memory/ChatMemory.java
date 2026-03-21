package com.example.n8n.services.Agent.Memory;

import java.util.List;

import com.example.n8n.models.workflow.AgentMessage;

public interface ChatMemory 
{
    void addMessage(String sessionKey,AgentMessage message);
    List<AgentMessage> getMessages(String sessionKey);    
    void clear(String sesseionKey);
    int getMessageCount(String sessionKey);

}
