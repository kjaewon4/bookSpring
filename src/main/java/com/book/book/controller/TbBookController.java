package com.book.book.controller;

import com.book.book.dto.BookDto;
import com.book.book.dto.BookWithKeywordsDTO;
import com.book.book.entity.*;
import com.book.book.repository.TbBookRepository;
import com.book.book.service.AuthenticationService;
import com.book.book.service.TbBookService;
import com.book.book.service.TbBookStoreService;
import com.book.book.service.TbRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import io.swagger.v3.oas.annotations.Parameter;


import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TbBookController {
    private final TbBookService tbBookService;
    private final TbBookRepository tbBookRepository;
    private final TbBookStoreService tbBookStoreService;
    private final TbRecommendService tbRecommendService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "메인페이지", description = "오늘 날짜의 뉴스 키워드 기반 도서 추천")
    @GetMapping("/")
    public  ResponseEntity<?> home(HttpServletRequest request) {
        LocalDate today = LocalDate.now(); // 오늘 날짜 가져오기
        List<BookDto> books = tbRecommendService.getRecommendedBooksByDate(today); // 비정적 메서드 호출

        if (books.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(books);

//        System.out.println(auth.getName());

//        // 인증 서비스 사용하여 userUuid를 추출
//        String userUuid = authenticationService.validateJwtAndExtractUserUuid(request);
//
//        System.out.println("home userUuid : " + userUuid);
//        // userUuid가 null이면 인증되지 않은 상태
//        if (userUuid == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
//        }
//
//        // 사용자 정보를 반환 (필요에 따라 추가 정보도 반환 가능)
//        return ResponseEntity.ok(Map.of("message", "로그인 성공", "userUuid", userUuid));

    }

    // http://localhost:8080/books/search?search=검색어, 도서 검색(제목) - 검색창 사용
    // full text index (n-gram parser 이용)쓸거임
    @Operation(summary = "도서 검색(제목) - 검색창 사용",
            description = "사용자가 입력한 키워드가 포함된 도서를 검색합니다. "
            + "제목의 일부를 입력하면 해당 단어를 포함하는 도서를 반환합니다. "
            + "예: '자바'를 입력하면 '자바의 정석', '모던 자바 인 액션' 등의 도서가 검색됩니다.")
    @GetMapping("books/search")
    public ResponseEntity<?> search(@RequestParam(name = "search") String search) {
        System.out.println("검색어 : " + search);

        try {
            // tb_books 테이블에서 받은 검색어를 books_title에 포함시키는 모든 도서 검색
            List<TbBook> books = tbBookRepository.findByBookTitleContainingIgnoreCase(search);

            if (books.isEmpty()) {
                // 없으면 해당 검색어에 해당하는 도서가 없음을 출력
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("해당 검색어에 해당하는 도서가 없습니다.");
            }

            System.out.println("검색 결과 : " + books);
            // 프론트엔드로 검색 결과 전송
            return ResponseEntity.ok(books);

        } catch (Exception e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("검색 중 오류가 발생했습니다.");
        }

    }

    // http://localhost:8080/books/category/소설
    // http://localhost:8080/books/category/{category}, 도서 카테고리별 조회 (에세이, 문학, 시...) - 버튼 사용
    @Operation(summary = "도서 카테고리별 조회 (에세이, 문학, 시...) - 버튼 사용", description = "도서 카테고리별 조회 (에세이, 문학, 시...) - 버튼 사용")
    @GetMapping("books/category/{category}")
    public ResponseEntity<?> searchByCategory(@PathVariable(name = "category") String category) {
        // tb_books 테이블에서 카테고리 일치하는거 다 가져와
        List<TbBook> result = tbBookRepository.findAllByBookCategory(category);

        if (!result.isEmpty()) {
            System.out.println("searchByCategory: " + result);
            return ResponseEntity.ok(result); // 200 OK + JSON 반환
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "해당 카테고리에 해당하는 도서가 없습니다."));
        }

    }

    // http://localhost:8080/book/9788920930720
    // 특정 ISBN의 도서 상세 정보 조회
    // 상세페이지에 키워드랑 알라딘 포함
    @Operation(summary = "특정 ISBN의 도서 상세 정보 조회", description = "주어진 ISBN을 기반으로 책 정보를 검색합니다. (예: 9788920930720)")
    @GetMapping("book/{isbn}")
    public Mono<ResponseEntity<BookWithKeywordsDTO>> getBookWithKeywords(
            @Parameter(description = "검색할 책의 ISBN 번호", example = "9788920930720")
            @PathVariable(name= "isbn") String isbn) {
        TbBook tbBook = tbBookService.getBookWithKeywords(isbn);

        BookWithKeywordsDTO bookWithKeywordsDTO = new BookWithKeywordsDTO(
                tbBook.getBookIsbn(),
                tbBook.getBookTitle(),
                tbBook.getBookPublisher(),
                tbBook.getBookAuthor(),
                tbBook.getBookImg(),
                tbBook.getBookDescription(),
                tbBook.getBookCategory(),
                tbBook.getKeywords().stream()
                        .map(TbBookKeyword::getBookKeyword)
                        .collect(Collectors.toList()) // 키워드 리스트로 변환

        );

        // 알라딘 API에서 매정 정보 가져오기


        // 키워드 포함 책 정보 리턴
        // TODO : 알라딘도 포함시켜서 리턴으로 수정
//        return ResponseEntity.ok(bookWithKeywordsDTO);
        return tbBookStoreService.fetchBookStores(isbn)
                .doOnNext(bookStoreResponse -> {
                    System.out.println("bookStoreResponse 내용: " + bookStoreResponse);
                })
                .flatMap(bookStoreResponse -> {
                    System.out.println("bookStoreResponse : " + bookStoreResponse.toString());
                    if (bookStoreResponse.getItemOffStoreList() != null) {
                        bookWithKeywordsDTO.setBookStores(bookStoreResponse.getItemOffStoreList());
                    } else {
                        System.out.println("itemOffStoreList is null!");
                    }
                    System.out.println("bookWithKeywordsDTO : " + bookWithKeywordsDTO.toString());
                    return Mono.just(ResponseEntity.ok(bookWithKeywordsDTO));
                });
    }
}
