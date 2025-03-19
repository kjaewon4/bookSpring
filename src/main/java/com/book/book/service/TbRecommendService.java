package com.book.book.service;

import com.book.book.dto.BookDto;
import com.book.book.entity.TbBookKeyword;
import com.book.book.entity.TbNewsKeyword;
import com.book.book.entity.TbRecommend;
import com.book.book.repository.TbBookKeywordRepository;
import com.book.book.repository.TbNewsKeywordRepository;
import com.book.book.repository.TbRecommendRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TbRecommendService {
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

        // 2. 조회된 뉴스 키워드들을 기준으로 TbRecommend 조회
        // tbRecommendRepository에 아래와 같이 메서드 정의
        // List<TbRecommend> findByNewsKeywordIn(List<TbNewsKeyword> newsKeywords);
        List<TbRecommend> recommendations = tbRecommendRepository.findByNewsKeywordIn(newsList);
        if (recommendations == null || recommendations.isEmpty()) {
            return List.of();
        }

        // 3. TbRecommend 에서 연결된 TbBook 추출 -> 중복 제거 -> DTO 변환
        List<BookDto> books = recommendations.stream()
                .map(TbRecommend::getBook)
                .distinct()  // TbBook의 equals/hashCode가 ISBN 기준으로 구현되어 있어야 함
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

        return books;
    }
}
