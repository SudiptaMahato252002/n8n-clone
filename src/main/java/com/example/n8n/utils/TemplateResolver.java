package com.example.n8n.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TemplateResolver 
{
    private final MustacheFactory mustacheFactory=new DefaultMustacheFactory();
    private final Pattern pattern=Pattern.compile("\\{\\{\\s*\\$([^}]+)\\s*\\}\\}");
    public String resolve(String template,Map<String,Object> context)
    {
        if(template==null||template.isEmpty())
        {
            return template;
        }
        try 
        {
            String mustcaheTemplate=convertToMustacheFormate(template);
            Mustache mustache=mustacheFactory.compile(new StringReader(mustcaheTemplate),"template");
            StringWriter writer=new StringWriter();
            mustache.execute(writer, context);

            return writer.toString();
            
        } 
        catch (Exception e) 
        {
            log.error("Error resolving template: {}", template, e);
            return template;
            
        }

    }

    private String convertToMustacheFormate(String template)
    {
        Matcher matcher=pattern.matcher(template);
        StringBuffer result=new StringBuffer();
        while (matcher.find()) 
        {
            String variable=matcher.group(1).trim();
            matcher.appendReplacement(result,"{{"+variable+"}}");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public boolean hasVariables(String template)
    {
        return template!=null&& template.contains("{{")&&template.contains("}}");
    }
}
