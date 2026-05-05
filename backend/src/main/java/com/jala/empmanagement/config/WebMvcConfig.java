package com.jala.empmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 * Maps /uploads/** URL path to the physical uploads directory on disk.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files via /uploads/** URL
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + System.getProperty("user.dir") + "/" + uploadDir);
    }
}
