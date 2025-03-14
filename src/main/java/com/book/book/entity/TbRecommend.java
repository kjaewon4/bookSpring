package com.book.book.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_recommend")
public class TbRecommend {

    // 뉴스 키워드랑 책 키워드 매핑용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recommendId;

    @ManyToOne
    @JoinColumn(name = "booksKeyword_id_1")
    private TbBookKeyword bookKeyword;  // ISBN

    @ManyToOne
    @JoinColumn(name = "news_id_1")
    private TbNewsKeyword newsKeyword;

}
