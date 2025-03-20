package com.book.book.service;


import com.book.book.dto.BookDetailDto;
import com.book.book.dto.BookDto;
import com.book.book.dto.TbBookStoreResponseDto;
import com.book.book.entity.TbBook;
import com.book.book.entity.TbBookKeyword;
import com.book.book.entity.TbNewsKeyword;
import com.book.book.repository.TbBookRepository;
import com.book.book.repository.TbNewsKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.data.domain.Pageable; // ✅ 올바른 Pageable (Spring Data)
import org.springframework.data.domain.Page; // ✅ Page도 함께 import

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TbBookService {
    private final TbBookRepository tbBookRepository;
    private final TbBookStoreService tbBookStoreService;
    private final TbNewsKeywordRepository tbNewsKeywordRepository;

    // 제목 검색 (페이징 포함)
    public Page<BookDto> searchBooksByTitle(String search, Pageable pageable) {
        Page<TbBook> books = tbBookRepository.findByBookTitleContainingIgnoreCase(search, pageable);
        return books.map(BookDto::new);
    }

    public Page<BookDto> getBooksByCategory(String category, Pageable pageable) {
        Page<TbBook> books = tbBookRepository.findAllByBookCategory(category, pageable);
        return books.map(BookDto::new); // ✅ 문제 없이 동작
    }


    // BookDetailDto 반환 메서드 (ISBN으로 조회)
    public Mono<ResponseEntity<BookDetailDto>> getBookDetailDtoByIsbn(String isbn) {
        // 1) 책 정보 가져오기
        return Mono.justOrEmpty(tbBookRepository.findByBookIsbn(isbn))
                .switchIfEmpty(Mono.error(new RuntimeException(isbn + "에 해당하는 도서를 찾을 수 없습니다.")))
                .flatMap(tbBook -> {
                    // BookDetailDto 생성
                    BookDetailDto bookDetailDto = BookDetailDto.builder()
                            .bookIsbn(tbBook.getBookIsbn())
                            .bookTitle(tbBook.getBookTitle())
                            .bookPublisher(tbBook.getBookPublisher())
                            .bookAuthor(tbBook.getBookAuthor())
                            .bookImg(tbBook.getBookImg())
                            .bookDescription(tbBook.getBookDescription())
                            .bookCategory(tbBook.getBookCategory())
                            .build();

                    // 비동기적으로 서점 정보 가져오기 (Reactive)
                    Mono<TbBookStoreResponseDto> bookStoreMono = tbBookStoreService.fetchBookStores(isbn)
                            .doOnNext(bookStoreResponse -> System.out.println("bookStoreResponse 내용: " + bookStoreResponse));

                    // 동기적으로 반환되는 뉴스 데이터를 Mono로 감싸기
                    Mono<List<TbNewsKeyword>> newsMono = Mono.fromCallable(() -> tbNewsKeywordRepository.findAllByBooksIsbn(isbn))
                            .subscribeOn(Schedulers.boundedElastic());

                    // 두 Mono를 병합하여 DTO 생성
                    return Mono.zip(bookStoreMono, newsMono)
                            .map(tuple -> {
                                TbBookStoreResponseDto bookStoreResponse = tuple.getT1();
                                List<TbNewsKeyword> newsList = tuple.getT2();

                                if (bookStoreResponse.getItemOffStoreList() != null) {
                                    bookDetailDto.setBookStores(bookStoreResponse.getItemOffStoreList());
                                } else {
                                    System.out.println("itemOffStoreList is null!");
                                }
                                bookDetailDto.setNewsList(newsList);
                                return ResponseEntity.ok(bookDetailDto);
                            });
                });




        // 1) 책 정보 가져오기
//        return Mono.justOrEmpty(tbBookRepository.findByBookIsbn(isbn))
//                .switchIfEmpty(Mono.error(new RuntimeException(isbn + "에 해당하는 도서를 찾을 수 없습니다.")))
//                .flatMap(tbBook -> {
//                    // 2) BookDetailDto 생성
//                    BookDetailDto bookDetailDto = BookDetailDto.builder()
//                            .bookIsbn(tbBook.getBookIsbn())
//                            .bookTitle(tbBook.getBookTitle())
//                            .bookPublisher(tbBook.getBookPublisher())
//                            .bookAuthor(tbBook.getBookAuthor())
//                            .bookImg(tbBook.getBookImg())
//                            .bookDescription(tbBook.getBookDescription())
//                            .bookCategory(tbBook.getBookCategory())
//                            .build();
//
//                    // 비동기적으로 알라딘 API에서 서점 정보 가져오기
//                    return tbBookStoreService.fetchBookStores(isbn)
//                            .doOnNext(bookStoreResponse -> System.out.println("bookStoreResponse 내용: " + bookStoreResponse))
//                            .map(bookStoreResponse -> {
//                                if (bookStoreResponse.getItemOffStoreList() != null) {
//                                    bookDetailDto.setBookStores(bookStoreResponse.getItemOffStoreList());
//                                } else {
//                                    System.out.println("itemOffStoreList is null!");
//                                }
//                                return ResponseEntity.ok(bookDetailDto);
//                            });
//                });
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
