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
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books/recommend")
@RequiredArgsConstructor
public class TbRecommendController {

    private final TbRecommendService tbRecommendService;
    private final TbBookKeywordRepository tbBookKeywordRepository;
    private final TbNewsKeywordRepository tbNewsKeywordRepository;
    private final TbRecommendRepository tbRecommendRepository;

    // http://localhost:8080/books/recommend/keyword/%EC%9D%B4%EB%B3%84
    // 뉴스 키워드 기반 도서 추천
    @Operation(summary = "뉴스 키워드 기반 도서 추천", description = "뉴스 키워드 기반 도서 추천")
    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<?> recommendBooksByKeyword(@PathVariable String keyword) {

        // 입력받은 뉴스 키워드로 TbNewsKeyword 엔티티를 조회
        // 해당 뉴스 키워드와 연결된 TbRecommend 엔티티들을 통해 관련 TbBook 목록을 가져옴

        // 1. 입력받은 keyword로 TbNewsKeyword 엔티티 조회
        TbNewsKeyword newsKeyword = tbNewsKeywordRepository.findByNewsKeyword(keyword);
        if(newsKeyword == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. 해당 뉴스 키워드와 매핑된 TbRecommend 엔티티들 조회
        List<TbRecommend> recommendations = tbRecommendRepository.findByNewsKeyword(newsKeyword);
        if(recommendations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 3. TbRecommend 에서 TbBook 추출 -> 중복 제거 -> DTO 변환
        // 순환 참조 날까봐 Dto로 변환
        List<BookDto> books = recommendations.stream()
                .map(TbRecommend::getBook) // 각 추천에서 연결된 TbBook 추출
                .distinct() // ISBN 기준 equals/hashCode가 올바르게 구현되어 있어야 중복 제거 가능
                .map(book -> {
                    // TbBook에 연결된 여러 TbBookKeyword에서 실제 키워드 문자열 리스트 생성
                    List<String> keywords = book.getKeywords().stream()
                            .map(TbBookKeyword::getBookKeyword)
                            .collect(Collectors.toList());
                    return new BookDto(
                            book.getBookIsbn(),
                            book.getBookTitle(),
                            book.getBookPublisher(),
                            book.getBookAuthor(),
                            book.getBookImg(),
                            book.getBookDescription(),
                            book.getBookCategory(),
                            keywords
                    );
                })
                .collect(Collectors.toList());

        // DTO 목록을 JSON 형식으로 리턴
        return ResponseEntity.ok(books);

    }

    // /books/recommend/news/date/{date}, ISO 표준 형식(예: "YYYY-MM-DD")으로 파싱되거나 포맷팅
    // 특정 날짜의 뉴스 키워드 기반 도서 추천
    @Operation(summary = "해당 날짜에 등록된 뉴스 키워드 기반 도서 추천", description = "해당 날짜에 등록된 뉴스 키워드 기반 도서 추천")
    @GetMapping("/news/date/{date}")

    public ResponseEntity<?> recommendBooksByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BookDto> books = tbRecommendService.getRecommendedBooksByDate(date);

        if (books.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(books);

    }

}
