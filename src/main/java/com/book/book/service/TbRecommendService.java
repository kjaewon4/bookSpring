package com.book.book.service;

import com.book.book.dto.BookDto;
import com.book.book.entity.TbBookKeyword;
import com.book.book.entity.TbNewsKeyword;
import com.book.book.entity.TbRecommend;
import com.book.book.repository.TbBookKeywordRepository;
import com.book.book.repository.TbNewsKeywordRepository;
import com.book.book.repository.TbRecommendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TbRecommendService {
    private final TbRecommendRepository recommendRepository;
    private final TbRecommendRepository tbRecommendRepository;
    private final TbBookKeywordRepository tbBookKeywordRepository;
    private final TbNewsKeywordRepository tbNewsKeywordRepository;

    public List<TbRecommend> findByNewsId(List<Long> newsIds) {
        return tbRecommendRepository.findByNewsKeyword_NewsIdIn(newsIds);
    }

    public List<BookDto> getRecommendedBooksByDate(LocalDate date) {

        // 1. 해당 날짜 뉴스 키워드 조회
        List<TbNewsKeyword> newsList = tbNewsKeywordRepository.findAllByNewsDate(date);
        if (newsList == null || newsList.isEmpty()) {
            return List.of();
        }

        // 2. 뉴스 키워드에서 newsId 리스트 추출
        List<Long> newsId = newsList.stream()
                .map(TbNewsKeyword::getNewsId)
                .collect(Collectors.toList());

        // 3. newsId를 기준으로 추천 도서 매핑 조회
        List<TbRecommend> recommendations = findByNewsId(newsId);
        if (recommendations == null || recommendations.isEmpty()) {
            return List.of();
        }

        // 4. booksKeywordId 리스트 추출
        List<Long> booksKeywordsIds = recommendations.stream()
                .map(rec -> rec.getBookKeyword().getBookKeywordId())
                .distinct()
                .collect(Collectors.toList());

        // 5. booksKeywordId를 기준으로 도서 ISBN 조회
        List<TbBookKeyword> bookKeywords = tbBookKeywordRepository.findByBookKeywordIdIn(booksKeywordsIds);
        if (bookKeywords == null || bookKeywords.isEmpty()) {
            return List.of();
        }

        // 6. 도서 정보를 DTO로 변환 후 반환
        return bookKeywords.stream()
                .map(TbBookKeyword::getBook)
                .distinct()
                .map(book -> new BookDto(
                        book.getBookIsbn(),
                        book.getBookTitle(),
                        book.getBookPublisher(),
                        book.getBookAuthor(),
                        book.getBookImg(),
                        book.getBookDescription(),
                        book.getBookCategory(),
                        book.getKeywords().stream()
                                .map(TbBookKeyword::getBookKeyword)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
