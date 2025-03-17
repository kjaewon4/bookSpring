package com.book.book.repository;

import com.book.book.dto.TbBookDto;
import com.book.book.entity.TbNewsKeyword;
import com.book.book.entity.TbRecommend;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TbRecommendRepository extends JpaRepository<TbRecommend, Long> {

    @Query("SELECT b FROM TbBook b " +
            "JOIN TbBookKeyword bk ON b.bookIsbn = bk.book.bookIsbn " +
            "JOIN TbRecommend r ON bk.bookKeywordId = r.bookKeyword.bookKeywordId " +
            "JOIN TbNewsKeyword  n ON r.newsKeyword.newsId = n.newsId " +
            "WHERE n.newsDate = :date")
    List<TbBookDto> findBooksByNewsDate(@Param("date") LocalDate date);

    List<TbRecommend> findByNewsKeyword_NewsIdIn(List<Long> newsIds);

    // 전달된 newsKeyword 객체의 식별자(ID)를 사용해서 해당 뉴스 키워드와 연관된 TbRecommend 레코드를 찾아줌
    // => 별도로 getNewsId()를 호출해서 ID를 전달할 필요 없이 객체 자체를 전달하면 자동으로 맞는 결과를 반환함
    List<TbRecommend> findByNewsKeyword(TbNewsKeyword newsKeyword);

    // 여러 뉴스 키워드에 해당하는 TbRecommend 조회
    List<TbRecommend> findByNewsKeywordIn(List<TbNewsKeyword> newsKeywords);

}
