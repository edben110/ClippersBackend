package com.clipers.clipers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir archivos estáticos desde la carpeta uploads
        // Optimizado para reducir carga del servidor
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(86400) // Cache por 24 horas (reduce peticiones)
                .resourceChain(true); // Habilita optimizaciones
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}