package com.rdpk.config;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.r2dbc.spi.ConnectionFactory;

@Configuration
public class DatabaseConfig {
    
    @Bean
    public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
        return DatabaseClient.create(connectionFactory);
    }
}

