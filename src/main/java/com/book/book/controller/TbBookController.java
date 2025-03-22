package com.book.book.controller;

import com.book.book.dto.BookDetailDto;
import com.book.book.dto.BookDto;
import com.book.book.service.PaginationService;
import com.book.book.service.TbBookService;
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
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TbBookController {
    private final TbBookService tbBookService;
    private final TbRecommendService tbRecommendService;
    private final PaginationService paginationService;

    @Operation(
            summary = "메인페이지",
            description = "오늘 날짜의 뉴스 키워드 기반 도서 추천을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 도서 목록 반환"),
                    @ApiResponse(responseCode = "404", description = "추천 도서가 없음")
            }
    )
    @GetMapping("")
    public  ResponseEntity<?> home(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LocalDate today = LocalDate.now(); // 오늘 날짜 가져오기
        Pageable pageable = PageRequest.of(page, size);

        // Page<BookDto>를 반환하도록 호출
        Page<BookDto> bookPage = tbRecommendService.getRecommendedBooksByDate(today, pageable);

        if (bookPage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // PaginationService를 사용하여 페이징 정보가 포함된 응답 Map 구성
        Map<String, Object> response = paginationService.createPaginatedResponse(bookPage);

        return ResponseEntity.ok(response);
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
    }

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
