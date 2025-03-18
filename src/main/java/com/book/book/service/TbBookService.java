package com.book.book.service;


import com.book.book.entity.TbBook;
import com.book.book.entity.TbBookKeyword;
import com.book.book.repository.TbBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TbBookService {
    private final TbBookRepository tbBookRepository;

    // TbBook을 조회한 후 자동으로 관련된 키워드들을 함께 가져오는 방식
    public TbBook getBookWithKeywords(String isbn) {
        TbBook tbBook = tbBookRepository.findByBookIsbn(isbn);

        /**
         * TbBook에서 @OneToMany 관계로 TbBookKeyword를 연결했기 때문에, tbBook.getKeywords()를 호출하면 연관된 키워드들이 자동으로 로딩됩니다.
         * 이때 Lazy Loading이 적용되어 있으므로, TbBook 객체가 실제로 조회될 때 관련된 **TbBookKeyword**들은 필요할 때 로딩됩니다.
         */
        // Lazy loading으로 keyword 가져옴
        List<TbBookKeyword> keywords = tbBook.getKeywords();  // 이 시점에서 키워드들이 Lazy Loading으로 가져와짐

        return tbBook;  // `tbBook` 객체는 이미 `keywords`를 포함하고 있음

    }
}
