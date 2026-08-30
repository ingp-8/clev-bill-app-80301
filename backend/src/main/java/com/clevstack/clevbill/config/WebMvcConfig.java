package com.clevstack.clevbill.config;

import com.clevstack.clevbill.security.PropertyAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final PropertyAccessInterceptor propertyAccessInterceptor;

    public WebMvcConfig(PropertyAccessInterceptor propertyAccessInterceptor) {
        this.propertyAccessInterceptor = propertyAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(propertyAccessInterceptor).addPathPatterns("/api/**");
    }
}
