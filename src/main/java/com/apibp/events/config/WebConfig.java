package com.apibp.events.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve docs/openapi.yaml at /openapi.yaml so Swagger UI (at /docs) renders our spec
        registry.addResourceHandler("/openapi.yaml")
                .addResourceLocations("classpath:/static/");
    }
}
