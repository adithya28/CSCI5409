package com.ad368540.ticTacToe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoDBConfig {
    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;
    @Bean
    public DynamoDbClient getDynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(amazonDynamoDBEndpoint))
                .build();
    }
    @Bean
    public DynamoDbEnhancedClient getDynamoDbEnhancedAsyncClient() {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(getDynamoDbClient())
                .build();
    }
}