package com.elibrary.book_service;

import com.elibrary.book_service.repository.elasticsearch.ElasticsearchRepo;
import com.elibrary.book_service.repository.jpa.TitleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BookServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner syncElasticsearch(TitleRepository jpaRepo, ElasticsearchRepo esRepo) {
		return args -> esRepo.saveAll(jpaRepo.findAll());
	}

}
