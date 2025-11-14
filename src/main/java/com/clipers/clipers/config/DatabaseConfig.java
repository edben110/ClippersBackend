package com.clipers.clipers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Singleton Pattern - Configuración central de BD MongoDB
 * Esta clase asegura que solo haya una configuración de base de datos
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.clipers.clipers.repository")
@EnableMongoAuditing
public class DatabaseConfig {

    private static DatabaseConfig instance;

    public DatabaseConfig() {
        instance = this;
    }

    public static DatabaseConfig getInstance() {
        return instance;
    }
}
