package com.elibrary.book_service;

import com.elibrary.book_service.repository.elasticsearch.ElasticsearchRepo;
import com.elibrary.book_service.service.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class BookServiceApplicationTests {

	@MockBean
	ElasticsearchRepo elasticsearchRepo;

	@MockBean
	MinioService minioService;

	@Test
	void contextLoads() {
	}

}
