package com.book.book.controller;
// 책 추천 및 조회

import com.book.book.dto.BookDto;
import com.book.book.entity.TbBookKeyword;
import com.book.book.entity.TbNewsKeyword;
import com.book.book.entity.TbRecommend;
import com.book.book.repository.TbBookKeywordRepository;
import com.book.book.repository.TbNewsKeywordRepository;
import com.book.book.service.TbRecommendService;
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

    // http://localhost:8080/books/recommend/keyword/%EC%9D%B4%EB%B3%84
    // 뉴스 키워드 기반 도서 추천
    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<?> recommendBooksByKeyword(@PathVariable String keyword) {
        // 키워드와 일치하는 모든 TbBookKeyword 엔티티를 조회 (여러 개일 수 있음)
         List<TbBookKeyword> keywordList  = tbBookKeywordRepository.findByBookKeyword(keyword);
        if (keywordList  == null) {
            return ResponseEntity.notFound().build();
        }
        // 각 TbBookKeyword에서 TbBook을 추출하고, 중복된 책이 있을 수 있으므로 distinct 처리
        // 순환 참조 날까봐 Dto로 변환
        List<BookDto> books = keywordList.stream()
                .map(TbBookKeyword::getBook) // 사용자 선택 키워드랑 일치하는 각 TbBookKeyword 객체에서 연결된 TbBook을 추출
                .distinct()  // TbBook 엔티티에 equals/hashCode 구현 필요 (ISBN 기준으로 구현하는 것이 좋음)
                .map(book -> {
                    // 한 도서에 연결된 여러 개의 키워드 객체(TbBookKeyword)에서 실제 키워드 문자열만 추출하여, 문자열 리스트(List<String>)로 만드는 작업 수행
                    // 도서 전체 키워드를 문자열 리스트로 반환
                    List<String> keywords = book.getKeywords().stream()  // 도서 객체(book)가 가지고 있는 키워드 리스트(List<TbBookKeyword>)를 가져옴
                            .map(TbBookKeyword::getBookKeyword)  // 각 TbBookKeyword 객체의 키워드 문자열을 추출
                            .collect(Collectors.toList());  // 추출된 키워드 문자열들을 리스트(List<String>)로 모아서 반환
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
    @GetMapping("/news/date/{date}")
    public ResponseEntity<?> recommendBooksByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // tb_newsKeyword → tb_recommend → tb_booksKeyword → tb_books 순으로 참조

        // 1. tb_newsKeyword 테이블에서 해당 날짜에 등록된 뉴스 키워드(TbNewsKeyword)를 조회
        List<TbNewsKeyword> newsList = tbNewsKeywordRepository.findAllByNewsDate(date);
        // 뽑은 newsList 객체 별로 newsId 뽑고
        // 뽑은 newsId에 매핑되는 tb_recommend 찾아서 bookkeyordId 착기
        // bookKetwordId로 bookkeyword 테이블에서 isbn 찾기
        // isbn으로 book 정보 가져오기 BookDto쓰면 될듯

        if (newsList == null || newsList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // 2. 뉴스 키워드에서 newsId 리스트 추출
        List<Long> newsId = newsList.stream()
                .map(TbNewsKeyword::getNewsId)
                .collect(Collectors.toList());

        // 3. newsId를 기준으로 tb_recommed에서 booksKeywordId 조회
        List<TbRecommend> recommendations = tbRecommendService.findByNewsId(newsId);
        if(recommendations == null || recommendations.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        // 4. booksKeywordId를 기준으로 booksIsbn 조회
        List<Long> booksKeywordsIds = recommendations.stream()
                .map(rec -> rec.getBookKeyword().getBookKeywordId())
                .distinct()
                .collect(Collectors.toList());

        List<TbBookKeyword> bookKeywords = tbBookKeywordRepository.findByBookKeywordIdIn(booksKeywordsIds);
        if(bookKeywords == null || bookKeywords.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 5. bookIsbn 기준으로 도서  정보를 조회하고 DTO로 변환
        List<BookDto> books = bookKeywords.stream()
                .map(TbBookKeyword::getBook)
                .distinct()
                .map(book -> new BookDto (
                    book.getBookIsbn(),
                    book.getBookTitle(),
                    book.getBookPublisher(),
                    book.getBookAuthor(),
                    book.getBookImg(),
                    book.getBookDescription(),
                    book.getBookCategory(),
                    book.getKeywords().stream().map(TbBookKeyword::getBookKeyword).collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(books);
    }

}
