package com.dynamiceventmanagement.databaseapp.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
@EnableSpringDataWebSupport
public class PageableConfigBean {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer customize() {
        return solve -> solve.setFallbackPageable(Pageable.unpaged());
    }
}