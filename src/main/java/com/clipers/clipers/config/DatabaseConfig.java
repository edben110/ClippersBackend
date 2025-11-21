package com.clipers.clipers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

/**
 * Singleton Pattern - Configuración central de BD MongoDB
 * Esta clase asegura que solo haya una configuración de base de datos
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.clipers.clipers.repository")
@EnableMongoAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class DatabaseConfig {

    private static DatabaseConfig instance;

    public DatabaseConfig() {
        instance = this;
    }

    public static DatabaseConfig getInstance() {
        return instance;
    }

    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(ZonedDateTime.now(ZoneId.of("America/Lima")).toLocalDateTime());
    }
}
