package com.book.book.controller;
// 책 추천 및 조회

import com.book.book.dto.BookDto;
import com.book.book.entity.TbBookKeyword;
import com.book.book.entity.TbNewsKeyword;
import com.book.book.entity.TbRecommend;
import com.book.book.repository.TbBookKeywordRepository;
import com.book.book.repository.TbNewsKeywordRepository;
import com.book.book.repository.TbRecommendRepository;
import com.book.book.service.TbRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class TbRecommendController {

    private final TbRecommendService tbRecommendService;
    private final TbBookKeywordRepository tbBookKeywordRepository;

    // http://localhost:8080/books/recommend/keyword/%EC%9D%B4%EB%B3%84
    // 뉴스 키워드 기반 도서 추천
    @Operation(
            summary = "뉴스 키워드 기반 도서 추천",
            description = "사용자가 선택한 뉴스 키워드를 기반으로 관련 도서를 추천합니다.\n"
                          + "검색어는 도서 키워드와 매핑되어 있으며, 관련 도서의 제목, 이미지, 저자, 출판사, 출판일, ISBN, 설명 및 추천 키워드가 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 도서 목록 반환"),
                    @ApiResponse(responseCode = "404", description = "해당 키워드에 해당하는 도서가 없음")
            }
    )
    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<?> recommendBooksByKeyword(
            @Parameter(description = "검색할 뉴스 키워드", example = "정치")
            @PathVariable String keyword) {

        List<TbBookKeyword> bookKeywords = tbBookKeywordRepository.findByBookKeyword(keyword);

        // TbBookKeyword에서 TbBook 추출, 중복 제거 및 BookDto 변환
        List<BookDto> books = bookKeywords.stream()
                .map(TbBookKeyword::getBook)
                .distinct()  // ISBN 기준 equals/hashCode가 올바르게 구현되어 있어야 함
                .map(book -> {
//                    List<String> keywords = book.getKeywords().stream()
//                            .map(TbBookKeyword::getBookKeyword)
//                            .collect(Collectors.toList());
                    return new BookDto(
                            book.getBookIsbn(),
                            book.getBookTitle(),
                            book.getBookPublisher(),
                            book.getBookAuthor(),
                            book.getBookImg(),
                            book.getBookDescription(),
                            book.getBookCategory()
//                            keywords
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(books);

    }

    // /books/recommend/news/date/{date}, ISO 표준 형식(예: "YYYY-MM-DD")으로 파싱되거나 포맷팅
    // 특정 날짜의 뉴스 키워드 기반 도서 추천
    @Operation(
            summary = "해당 날짜에 등록된 뉴스 키워드 기반 도서 추천",
            description = "입력된 날짜에 등록된 뉴스 키워드를 기반으로 도서를 추천합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 도서 목록 반환"),
                    @ApiResponse(responseCode = "404", description = "해당 날짜에 등록된 뉴스 키워드가 없거나 추천 도서가 없음")
            }
    )
    @GetMapping("/news/date/{date}")
    public ResponseEntity<?> recommendBooksByDate(
            @Parameter(description = "검색할 날짜 (YYYY-MM-DD 형식)", example = "2025-03-17")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BookDto> books = tbRecommendService.getRecommendedBooksByDate(date);

        if (books.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(books);

    }

}
