package com.book.book.dto;

import com.book.book.entity.TbNewsKeyword;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// 책 정보 (tb_book) + 뉴스 카테고리 (newsCategory) + 서점 정보 (TbBookStoreDto)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BookDetailDto {

    private String bookIsbn;
    private String bookTitle;
    private String bookAuthor;
    private String bookPublisher;
    private String bookCategory;
    private String bookDescription;
    private String bookImg;

    private List<TbBookStoreDto> bookStores = new ArrayList<>();  // 서점 정보 리스트, null 방지
    // 추가된 뉴스 테이블 전체 데이터를 담기 위한 필드
    private List<TbNewsKeyword> newsList = new ArrayList<>();
}
