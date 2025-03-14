package com.book.book.service;

import com.book.book.dto.TbBookDto;
import com.book.book.entity.TbRecommend;
import com.book.book.repository.TbRecommendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TbRecommendService {
    private final TbRecommendRepository recommendRepository;
    private final TbRecommendRepository tbRecommendRepository;

    public List<TbRecommend> findByNewsId(List<Long> newsIds) {
        return tbRecommendRepository.findByNewsKeyword_NewsIdIn(newsIds);
    }
}
