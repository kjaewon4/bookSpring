package com.book.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
// 추천 할 때 보낼 데이터

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {
    private String bookIsbn;
    private String bookTitle;
    private String bookPublisher;
    private String bookAuthor;
    private String bookImg;
    private String bookDescription;
    private String bookCategory;
    // TbBookKeyword 엔티티의 bookKeyword 필드를 문자열 리스트로 변환하여 담음
//    private List<String> keywords;
}
