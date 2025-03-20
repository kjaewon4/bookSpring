package com.book.book.controller;

import com.book.book.dto.BookDetailDto;
import com.book.book.dto.BookDto;
import com.book.book.entity.*;
import com.book.book.repository.TbBookRepository;
import com.book.book.service.TbBookService;
import com.book.book.service.TbBookStoreService;
import com.book.book.service.TbRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @Operation(
            summary = "메인페이지",
            description = "오늘 날짜의 뉴스 키워드 기반 도서 추천을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 도서 목록 반환"),
                    @ApiResponse(responseCode = "404", description = "추천 도서가 없음")
            }
    )
    @GetMapping("")
    public  ResponseEntity<?> home() {
        LocalDate today = LocalDate.now(); // 오늘 날짜 가져오기
        List<BookDto> books = tbRecommendService.getRecommendedBooksByDate(today); // 비정적 메서드 호출

        if (books.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(books);
    }

    // http://localhost:8080/books/search?search=검색어, 도서 검색(제목) - 검색창 사용
    // full text index (n-gram parser 이용)쓸거임
    @Operation(
            summary = "도서 검색(제목)",
            description = "사용자가 입력한 글자가 포함된 도서를 검색합니다. 예를 들어 '그림'을 입력하면 '그림 속에서 나를 만나다 (자화상에서 내 마음 치유하기)' 등의 도서를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "검색 결과 도서 목록 반환"),
                    @ApiResponse(responseCode = "404", description = "해당 검색어에 해당하는 도서가 없음")
            }
    )
    @GetMapping("books/search")
    public ResponseEntity<?> search(
            @Parameter(description = "검색할 도서 제목", example = "그림")
            @RequestParam(name = "search") String search,
            @RequestParam(defaultValue = "0") int page,  // 기본 페이지 0
            @RequestParam(defaultValue = "20") int size  // 한 페이지당 20개씩
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDto> bookList = tbBookService.searchBooksByTitle(search, pageable);

        if (bookList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 검색어에 해당하는 도서가 없습니다.");
        }

        return ResponseEntity.ok(bookList);

//        System.out.println("검색어 : " + search);
//
//        try {
//            // tb_books 테이블에서 받은 검색어를 books_title에 포함시키는 모든 도서 검색
//            List<TbBook> books = tbBookRepository.findByBookTitleContainingIgnoreCase(search);
//            List<BookDto> bookList = tbBookService.getBookDto(books);
//
//            if (bookList.isEmpty()) {
//                // 없으면 해당 검색어에 해당하는 도서가 없음을 출력
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body("해당 검색어에 해당하는 도서가 없습니다.");
//            }
//
//            System.out.println("검색 결과 : " + bookList);
//            // 프론트엔드로 검색 결과 전송
//            return ResponseEntity.ok(bookList);
//
//        } catch (Exception e) {
//            // 예외 처리
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("검색 중 오류가 발생했습니다.");
//        }

    }

    // http://localhost:8080/books/category/소설
    // http://localhost:8080/books/category/{category}, 도서 카테고리별 조회 (에세이, 문학, 시...) - 버튼 사용
    @Operation(
            summary = "도서 카테고리별 조회",
            description = "선택한 카테고리에 해당하는 도서를 조회합니다. 예를 들어 '인문과학'을 선택하면 인문과학 분야의 모든 도서를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "카테고리별 도서 목록 반환"),
                    @ApiResponse(responseCode = "404", description = "해당 카테고리에 해당하는 도서가 없음")
            }
    )
    @GetMapping("books/category/{category}")
    public ResponseEntity<?> searchByCategory(
            @Parameter(description = "조회할 도서 카테고리", example = "인문과학")
            @PathVariable(name = "category") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDto> bookList = tbBookService.getBooksByCategory(category, pageable);

        if (bookList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 카테고리에 해당하는 도서가 없습니다.");
        }

        return ResponseEntity.ok(bookList);

//
//        // tb_books 테이블에서 카테고리 일치하는거 다 가져와
//        List<TbBook> books = tbBookRepository.findAllByBookCategory(category);
//        List<BookDto> bookList = tbBookService.getBookDto(books);
//
//
//        if (!bookList.isEmpty()) {
//            System.out.println("searchByCategory: " + bookList);
//            return ResponseEntity.ok(bookList); // 200 OK + JSON 반환
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Collections.singletonMap("message", "해당 카테고리에 해당하는 도서가 없습니다."));
//        }

    }


    // http://localhost:8080/book/9788920930720
    // 특정 ISBN의 도서 상세 정보 조회
    // 상세페이지에 키워드랑 알라딘 포함
    @Operation(
            summary = "특정 ISBN의 도서 상세 정보 조회",
            description = "주어진 ISBN을 기반으로 도서의 상세 정보를 검색합니다.\n\n예시 ISBN: 9788920930720",
            responses = {
                    @ApiResponse(responseCode = "200", description = "도서 상세 정보 반환"),
                    @ApiResponse(responseCode = "404", description = "해당 ISBN의 도서가 없음")
            }
    )
    @GetMapping("book/{isbn}")
    public ResponseEntity<BookDetailDto> getBookDetailsByIsbn(
            @Parameter(description = "검색할 도서의 ISBN 번호", example = "9791188366170")
            @PathVariable(name= "isbn") String isbn
    ) {

        ResponseEntity<BookDetailDto> responseEntity = tbBookService.getBookDetailDtoByIsbn(isbn).block();

        return responseEntity;

    }

}
