package com.book.book.repository;

import com.book.book.entity.TbNewsKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TbNewsKeywordRepository extends JpaRepository<TbNewsKeyword, Integer> {
    List<TbNewsKeyword> findAllByNewsDate(LocalDate newsDate);
}
