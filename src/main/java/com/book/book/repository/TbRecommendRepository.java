package com.book.book.repository;

import com.book.book.dto.TbBookDto;
import com.book.book.entity.TbRecommend;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
