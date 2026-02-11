package com.musmike.familyheritage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    private final CorsProperties corsProperties;

    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                var origins = corsProperties.getAllowedOrigins();
                var methods = corsProperties.getAllowedMethods();

                if (origins != null && !origins.isEmpty()) {
                    registry.addMapping("/**")
                            .allowedOrigins(origins.toArray(new String[0]))
                            .allowedMethods(methods != null ? methods.toArray(new String[0]) : new String[]{"GET", "POST"})
                            .allowedHeaders("*")
                            .allowCredentials(corsProperties.isAllowCredentials());
                }
            }
        };
    }
}
