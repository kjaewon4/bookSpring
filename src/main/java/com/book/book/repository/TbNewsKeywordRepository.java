package com.book.book.repository;

import com.book.book.entity.TbNewsKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TbNewsKeywordRepository extends JpaRepository<TbNewsKeyword, Integer> {
    // newsDate 컬럼에 맞게 날짜로 조회 (예: LocalDate 변환 또는 DATE 타입 컬럼)
    List<TbNewsKeyword> findAllByNewsDate(LocalDate newsDate);

    TbNewsKeyword findByNewsKeyword(String newsKeyword);
}
