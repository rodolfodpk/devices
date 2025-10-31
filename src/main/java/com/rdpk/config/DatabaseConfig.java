package com.rdpk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.rdpk.device.repository")
public class DatabaseConfig {
    // Spring Data R2DBC automatically handles repository creation
    // DatabaseClient bean removed as it's no longer needed
}

