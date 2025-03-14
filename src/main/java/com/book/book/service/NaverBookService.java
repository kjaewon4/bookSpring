package com.book.book.service;

import com.book.book.dto.NaverApiDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NaverBookService {

    private final WebClient webClient;

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${naver.client.secret}")
    private String naverClientSecret;

    public NaverBookService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://openapi.naver.com/v1/search").build();
    }

    public Mono<NaverApiDto> searchBooks(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/book_adv.xml")
                        .queryParam("d_isbn", query)
                        .queryParam("display", 10)
                        .queryParam("start", 1)
                        .queryParam("sort", "sim")
                        .build())
                .header("X-Naver-Client-Id", naverClientId)
                .header("X-Naver-Client-Secret", naverClientSecret)
                .accept(MediaType.APPLICATION_XML)  // XML 데이터 요청
                .retrieve()
                .bodyToMono(NaverApiDto.class); // XML 응답을 NaverApiDto로 변환하여 Mono로 반환
    }

}

