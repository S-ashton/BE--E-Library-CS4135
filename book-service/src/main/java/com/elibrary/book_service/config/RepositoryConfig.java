package com.elibrary.book_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.elibrary.book_service.repository.jpa"
)
@EnableElasticsearchRepositories(
    basePackages = "com.elibrary.book_service.repository.elasticsearch"
)
public class RepositoryConfig {

}