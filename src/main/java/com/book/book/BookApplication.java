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

//	@Override
//	public void run(String... args) throws Exception {
//		if (args.length > 0) {
//			String itemId = args[0];  // 첫 번째 파라미터로 itemId를 가져옴
//
//			// 비동기 메서드를 호출하고, subscribe()로 실행을 시작합니다.
//			tbBookStoreService.fetchAndSaveData(itemId)
//					.doOnTerminate(() -> System.out.println("API 호출 및 데이터 저장 작업이 종료되었습니다."))
//					.subscribe(); // 비동기 처리 시작
//		} else {
//			System.out.println("itemId 파라미터가 필요합니다.");
//		}
//	}

}
