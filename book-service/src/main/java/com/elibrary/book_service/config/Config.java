package com.elibrary.book_service.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "com.elibrary.book_service.service" })
public class Config {
}
