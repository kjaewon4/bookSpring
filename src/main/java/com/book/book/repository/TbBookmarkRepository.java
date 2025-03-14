package com.book.book.repository;

import com.book.book.entity.TbBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.awt.print.Book;
import java.util.List;
import java.util.Optional;

public interface TbBookmarkRepository extends JpaRepository<TbBookmark, Long> {

    TbBookmark findByBookBookIsbn(String bookIsbn); // TbBook의 bookIsbn 필드로 조회

    List<Book> findAllByUserUserUuid(String UserUuid);
    List<TbBookmark> findAllByUserUserId(Long userId);


    Optional<TbBookmark> findByBookBookIsbnAndUserUserId(String bookIsbn, Long userId);
}
