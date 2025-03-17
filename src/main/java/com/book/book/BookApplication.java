package com.book.book;

import com.book.book.service.TbBookStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.book.book.repository")
@SpringBootApplication(scanBasePackages = "com.book.book")
public class BookApplication
//		implements CommandLineRunner
{
	@Autowired
	private TbBookStoreService tbBookStoreService;

    public static void main(String[] args) {
		SpringApplication.run(BookApplication.class, args);
	}


}
