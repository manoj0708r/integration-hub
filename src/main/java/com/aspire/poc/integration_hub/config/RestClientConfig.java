package com.aspire.poc.integration_hub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {

        HttpClient httpClient = HttpClient.newBuilder().version(Version.HTTP_1_1).build();

        return RestClient.builder().requestFactory(new JdkClientHttpRequestFactory(httpClient)).build();
    }
}