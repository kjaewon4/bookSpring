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

//
//import com.book.book.dto.TbBookDto;
//import com.book.book.entity.TbBook;
//import com.book.book.repository.TbBookRepository;
//import lombok.RequiredArgsConstructor;
//import org.reactivestreams.Publisher;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class TbBookService {
////    private WebClient webClient = WebClient.builder().baseUrl("https://openapi.naver.com").build();
////    private final TbBookRepository tbBookRepository;
////
//    @Value("${naver.client.id}")
//    private String clientId;
//
//    @Value("${naver.client.secret}")
//    private String clientSecret;
////
////    // 모든 책을 가져와서 DB에 저장
////    public void fetchAndSaveAllBooks(String query) {
////        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
////        int display = 90;  // 한 번에 100개씩 요청 가능
////
////        System.out.println(encodedQuery);
////        // 첫 번째 요청 보내고 total 값 확인
////        fetchBooks(encodedQuery, 1, display)
////                .flatMap(total -> {
////                    if (total == 0) {
////                        return Mono.empty();  // total이 0일 경우 더 이상 요청하지 않음
////                    }
////
////                    int totalPages = (int) Math.ceil((double) total / display);
////                    System.out.println("총 검색 결과 개수: " + total);
////
////                    // 각 페이지에 대해 API 요청 (Flux를 반환하여 각 페이지에 대해 비동기적으로 처리)
////                    return Flux.range(1, totalPages)
////                            .flatMap(page -> fetchAndSaveBooks(encodedQuery, page, display))
////                            .collectList()  // Flux를 List로 변환하여 Mono<List> 반환
////                            .flatMap(list -> Mono.empty());  // 모든 작업이 끝난 후 처리
////                })
////                .subscribe();
////    }
////
////    // API 요청 보내고 검색 결과 개수 반환
////    private Mono<Integer> fetchBooks(String query, int start, int display) {
////        return webClient.get()
////                .uri(uriBuilder -> uriBuilder
////                        .path("/v1/search/book.json")
////                        .queryParam("query", query)
////                        .queryParam("start", start)
////                        .queryParam("display", display)
////                        .build())
////                .headers(headers -> {
////                    headers.set("X-Naver-Client-Id", clientId);
////                    headers.set("X-Naver-Client-Secret", clientSecret);
////                })
////                .retrieve() // 서버에 HTTP 요청을 보낸 후, 응답을 받아옴.  Mono 또는 Flux 타입 반환
////                .bodyToMono(TbBookDto.class)  // JSON 응답을 DTO로 변환
////                .doOnSuccess(response -> System.out.println("응답 받은 데이터: " + response))  // 성공적으로 응답 받은 후 출력
////                .doOnError(error -> System.out.println("오류 발생: " + error.getMessage()))  // 오류 발생 시 출력
////                .map(response -> response.getTotal());  // API 응답에서 'total' 값을 반환
////    }
////
////    // 검색 결과를 DB에 저장하는 메서드
////    private Mono<Void> fetchAndSaveBooks(String query, int start, int display) {
////        return webClient.get()
////                .uri(uriBuilder -> uriBuilder
////                        .path("/v1/search/book.json")
////                        .queryParam("query", query)
////                        .queryParam("start", start)
////                        .queryParam("display", display)
////                        .build())
////                .headers(headers -> {
////                    headers.set("X-Naver-Client-Id", clientId);
////                    headers.set("X-Naver-Client-Secret", clientSecret);
////                })
////                .retrieve()
////                .bodyToMono(TbBookDto.class)  // JSON 응답을 DTO로 변환
////                .flatMap(response -> {
////                    List<TbBook> books = response.toEntityList();  // DTO를 Entity로 변환
////                    // Flux.fromIterable(books)를 Mono<Void>로 변환하기 위해 saveAll을 처리하고, Mono.empty() 반환
////                    return Mono.fromRunnable(() -> tbBookRepository.saveAll(books));  // DB에 저장하는 작업을 Mono로 감쌈
////                });
////    }
//
//    public Mono<String> fetchAndSaveData(String query) {
//
//        // UTF-8로 검색어 인코딩
//        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
//
//        WebClient webClient = WebClient.builder()
//                .baseUrl("https://openapi.naver.com")
//                .build();
//
//        return webClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/v1/search/book.json")
//                        .queryParam("query", encodedQuery)
//                        .build())
//                .headers(headers -> {
//                    headers.set("X-Naver-Client-Id", clientId);
//                    headers.set("X-Naver-Client-Secret", clientSecret);
//
//                })
//                .retrieve()
//                .bodyToMono(String.class)
//                .doOnNext(response -> System.out.println("응답 데이터: " + response));
//
////        String url = ""
//    }
//
//}
