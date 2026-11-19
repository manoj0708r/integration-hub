package com.aspire.poc.integration_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJms
@EnableScheduling
@SpringBootApplication
public class IntegrationHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntegrationHubApplication.class, args);
	}

}
