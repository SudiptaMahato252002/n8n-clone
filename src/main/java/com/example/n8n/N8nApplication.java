package com.example.n8n;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class N8nApplication {

	public static void main(String[] args) 
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(N8nApplication.class, args);
	}

}
