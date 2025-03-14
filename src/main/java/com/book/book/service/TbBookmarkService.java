package com.book.book.service;

import com.book.book.entity.TbBookmark;
import com.book.book.repository.TbBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TbBookmarkService {


    private final TbBookmarkRepository tbBookmarkRepository;

    public List<Book> getBookmarksByUserUuid(String uuid) {
        List<Book> books = tbBookmarkRepository.findAllByUserUserUuid(uuid);
        return books;
    }
}
