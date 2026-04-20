package com.elibrary.book_service;

import com.elibrary.book_service.messaging.BookEventPublisher;
import com.elibrary.book_service.repository.elasticsearch.ElasticsearchRepo;
import com.elibrary.book_service.repository.jpa.TitleRepository;
import com.elibrary.book_service.service.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
class BookServiceApplicationTests {

	@MockitoBean
	ElasticsearchRepo elasticsearchRepo;

	@MockitoBean
	MinioService minioService;

	@MockitoBean
	BookEventPublisher bookEventPublisher;

	@MockitoBean
	TitleRepository titleRepository;

	@Test
	void contextLoads() {
		when(titleRepository.findAll()).thenReturn(List.of());
	}

}
