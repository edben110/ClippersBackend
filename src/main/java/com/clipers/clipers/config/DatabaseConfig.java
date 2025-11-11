package com.clipers.clipers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

/**
 * Singleton Pattern - Configuración central de BD
 * Esta clase asegura que solo haya una configuración de base de datos
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.clipers.clipers.repository")
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String databaseDriver;

    private static DatabaseConfig instance;

    public DatabaseConfig() {
        instance = this;
    }

    public static DatabaseConfig getInstance() {
        return instance;
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(databaseUrl)
                .username(databaseUsername)
                .password(databasePassword)
                .driverClassName(databaseDriver)
                .build();
    }
}
