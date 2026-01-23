package com.example.n8n.services.Node.Executors;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.n8n.enums.Platform;
import com.example.n8n.exceptions.NodeExecutionException;
import com.example.n8n.models.workflow.ExecutionContext;
import com.example.n8n.models.workflow.WorkflowNode;
import com.example.n8n.services.Credentials.CredentialService;
import com.example.n8n.services.Node.NodeExecutor;
import com.example.n8n.utils.TemplateResolver;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNodeExecutors implements NodeExecutor 
{
    private final CredentialService credentialService;
    private final TemplateResolver templateResolver;
    @Override
    public Platform getSuportedPlatform() 
    {
        return Platform.RESEND_EMAIL;
    }

    @Override
    public Object execute(WorkflowNode node, ExecutionContext context, String credentialsId) 
    {
        try 
        {
            Map<String,Object> credentials=credentialService.getDecryptedCredentials(credentialsId);
            String apiKey=(String)credentials.get("apiKey");
            String fromEmail=(String)credentials.get("fromEmail");

            if (apiKey == null || fromEmail == null) 
            {
                throw new NodeExecutionException("Email credentials missing apiKey or fromEmail");
            }

            Map<String,String> config=node.getConfig();
            String toTemplate=config.get("to");
            String bodyTemplate=config.get("body");
            String subjectTemplate=config.get("subject");

            Map<String,Object> contextMap=context.toMap();
            String to=templateResolver.resolve(toTemplate, contextMap);
            String subject=templateResolver.resolve(subjectTemplate, contextMap);
            String body=templateResolver.resolve(bodyTemplate, contextMap);
            
            log.debug("Sending email to: {}, subject: {}", to, subject);

            Resend resend=new Resend(apiKey);
            CreateEmailOptions params=CreateEmailOptions.builder()
                .from(fromEmail)
                .to(to)
                .subject(subject)
                .html(body)                            
                .build();
            
            CreateEmailResponse response=resend.emails().send(params);
            log.info("Email sent successfully: messageId={}", response.getId());

            Map<String,Object> result=new HashMap<>();
            result.put("to", to);
            result.put("subject", subject);
            result.put("messageId", response.getId());
            result.put("status", "sent");
        
            return response;
        } 
        catch(ResendException e)
        {
            log.error("Failed to send email: {}", e.getMessage(), e);
            throw new NodeExecutionException("Email send failed: " + e.getMessage(), e);
        }
        catch (Exception e) 
        {
            log.error("Error executing email node: {}", e.getMessage(), e);
            throw new NodeExecutionException("Email node execution failed: " + e.getMessage(), e);
        }   
    }
    
}
