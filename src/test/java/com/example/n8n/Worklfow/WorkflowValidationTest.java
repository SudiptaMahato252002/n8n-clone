package com.example.n8n.Worklfow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.n8n.services.Workflow.WorkflowValidationService;

public class WorkflowValidationTest 
{
    @Test
    void testValidateWorkflow() {

        WorkflowValidationService service = new WorkflowValidationService();

        // -----------------------
        // NODES (7 total)
        // -----------------------

        Map<String, Object> nodes = new HashMap<>();

        for (int i = 1; i <= 7; i++) {
            Map<String, Object> node = new HashMap<>();
            node.put("type", "DUMMY_TYPE");
            Map<String,String> config=Map.of("data1","info-data1","data2","info-data2");
            node.put("config",config);
            nodes.put("node_" + i, node);
            
        }

        // -----------------------
        // CONNECTIONS
        // -----------------------

        Map<String, Object> connections = new HashMap<>();

        // node_1 -> node_2, node_3, node_5
        connections.put("node_1", List.of("node_2", "node_3", "node_5"));

        // node_2 -> node_4
        connections.put("node_2", List.of("node_1"));

        // node_3 -> node_6
        connections.put("node_3", List.of("node_6"));

        // node_6 -> node_7
        connections.put("node_6", List.of("node_7"));

        // -----------------------
        // VALIDATION
        // -----------------------

        assertDoesNotThrow(() -> {
            service.validateWorkflow(nodes, connections);
        });
    }

    
}
