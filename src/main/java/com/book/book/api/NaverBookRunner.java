package com.book.book.api;

import com.book.book.dto.LibraryApiDto;
import com.book.book.dto.NaverApiDto;
import com.book.book.entity.TbBook;
import com.book.book.repository.TbBookRepository;
import com.book.book.service.LibraryApiService;
import com.book.book.service.NaverBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class NaverBookRunner implements CommandLineRunner {
    private final LibraryApiService libraryApiService;
    private final NaverBookService naverBookService;
    private final TbBookRepository tbBookRepository;

    @Autowired
    public NaverBookRunner(NaverBookService naverBookService, TbBookRepository tbBookRepository, LibraryApiService libraryApiService) {
        this.naverBookService = naverBookService;
        this.tbBookRepository = tbBookRepository;
        this.libraryApiService = libraryApiService;
    }

    @Override
    public void run(String... args) {
        // 검색어 리스트
        List<String> recomisbnList = libraryApiService.getRecomisbn().block(); // 동기적으로 결과 기다림

        if (recomisbnList != null) {
            // 각 recomisbn을 NaverBookService로 전달하여 책 정보를 가져오기
            for (String recomisbn : recomisbnList) {
                // Mono에서 동기적으로 값 받기, Mono에 대해 block()을 호출해야 함
                NaverApiDto naverApiDto = naverBookService.searchBooks(recomisbn).block(); // block() 사용하여 동기적으로 결과를 받음

                if (naverApiDto != null && naverApiDto.getChannel() != null && naverApiDto.getChannel().getItems() != null) {
                    // NaverApiDto에서 책 정보 처리
                    naverApiDto.getChannel().getItems().forEach(naverBook -> {
                        TbBook book = new TbBook(
                                naverBook.getTitle(),
                                naverBook.getImage(),
                                naverBook.getAuthor(),
                                naverBook.getPublisher(),
                                naverBook.getIsbn(),
                                naverBook.getDescription()
                        );

                        // 책 정보를 DB에 저장
                        tbBookRepository.save(book);
                        System.out.println("저장된 책: [" + recomisbn + "] - " + book.getBookTitle());
                    });
                } else {
                    System.out.println("Naver API에서 검색된 결과가 없습니다: " + recomisbn);
                }
            }
        } else {
            System.err.println("Library API에서 검색된 결과가 없습니다.");
        }
    }
}





