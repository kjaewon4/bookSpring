package com.book.book.service;


import com.book.book.dto.BookDetailDto;
import com.book.book.dto.BookDto;
import com.book.book.entity.TbBook;
import com.book.book.entity.TbBookKeyword;
import com.book.book.repository.TbBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TbBookService {
    private final TbBookRepository tbBookRepository;
    private final TbBookStoreService tbBookStoreService;


    // BookDetailDto 반환 메서드 (ISBN으로 조회)
    public Mono<ResponseEntity<BookDetailDto>> getBookDetailDtoByIsbn(String isbn) {
        return Mono.justOrEmpty(tbBookRepository.findByBookIsbn(isbn))
                .switchIfEmpty(Mono.error(new RuntimeException(isbn + "에 해당하는 도서를 찾을 수 없습니다.")))
                .flatMap(tbBook -> {
                    // BookDetailDto 변환
                    BookDetailDto bookDetailDto = BookDetailDto.builder()
                            .bookIsbn(tbBook.getBookIsbn())
                            .bookTitle(tbBook.getBookTitle())
                            .bookPublisher(tbBook.getBookPublisher())
                            .bookAuthor(tbBook.getBookAuthor())
                            .bookImg(tbBook.getBookImg())
                            .bookDescription(tbBook.getBookDescription())
                            .bookCategory(tbBook.getBookCategory())
                            .newsCategory("미분류")  // TODO: 뉴스 카테고리 말고 뉴스 테이블 다 넣어
                            .build();

                    // 비동기적으로 알라딘 API에서 서점 정보 가져오기
                    return tbBookStoreService.fetchBookStores(isbn)
                            .doOnNext(bookStoreResponse -> System.out.println("bookStoreResponse 내용: " + bookStoreResponse))
                            .map(bookStoreResponse -> {
                                if (bookStoreResponse.getItemOffStoreList() != null) {
                                    bookDetailDto.setBookStores(bookStoreResponse.getItemOffStoreList());
                                } else {
                                    System.out.println("itemOffStoreList is null!");
                                }
                                return ResponseEntity.ok(bookDetailDto);
                            });
                });
    }


    public List<BookDto> getBookDto(List<TbBook> bookList) {
        return bookList.stream()
                .map(tb -> new BookDto(
                        tb.getBookIsbn(),
                        tb.getBookTitle(),
                        tb.getBookPublisher(),
                        tb.getBookAuthor(),
                        tb.getBookImg(),
                        tb.getBookDescription(),
                        tb.getBookCategory()))
                .collect(Collectors.toList());
    }


    // TbBook을 조회한 후 자동으로 관련된 키워드들을 함께 가져오는 방식
    public TbBook getBookWithKeywords(String isbn) {
        TbBook tbBook = tbBookRepository.findByBookIsbn(isbn)
                .orElseThrow(() -> new RuntimeException(isbn + "에 해당하는 도서를 찾을 수 없습니다."));
        /**
         * TbBook에서 @OneToMany 관계로 TbBookKeyword를 연결했기 때문에, tbBook.getKeywords()를 호출하면 연관된 키워드들이 자동으로 로딩됩니다.
         * 이때 Lazy Loading이 적용되어 있으므로, TbBook 객체가 실제로 조회될 때 관련된 **TbBookKeyword**들은 필요할 때 로딩됩니다.
         */
        // Lazy loading으로 keyword 가져옴
        List<TbBookKeyword> keywords = tbBook.getKeywords();  // 이 시점에서 키워드들이 Lazy Loading으로 가져와짐

        return tbBook;  // `tbBook` 객체는 이미 `keywords`를 포함하고 있음

    }
}
