package com.example.n8n.models.workflow;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeConfig 
{
    private Map<String,String> fields;

    public String get(String key) {
        return fields!=null?fields.get(key):null;
    }

    public void put(String key, String value) {
        if(fields!=null) {
            fields.put(key, value);
        }
    }
    
}
