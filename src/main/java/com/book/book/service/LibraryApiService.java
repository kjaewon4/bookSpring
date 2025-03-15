package com.book.book.service;

import com.book.book.dto.LibraryApiDto;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibraryApiService {

    private final WebClient webClient;

    @Value("${openapi.key}")
    private String apiKey;

    public LibraryApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://nl.go.kr/NL/search/openApi")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public Mono<List<String>> getRecomisbn() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/saseoApi.do")
                        .queryParam("key", apiKey)
                        .queryParam("startRowNumApi", "1")
                        .queryParam("endRowNumApi", "500")
                        .build())
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .bodyToMono(String.class)  // XML을 String으로 받음
//                .doOnNext(xmlResponse -> System.out.println("응답 받은 XML:\n" + xmlResponse))
                .flatMap(xmlResponse -> {
                    try {
                        LibraryApiDto parsedResponse = parseXml(xmlResponse);
                        // listElements가 null이면 빈 리스트 반환
                        List<String> isbnList = Optional.ofNullable(parsedResponse.getListElements())
                                .orElse(List.of())
                                .stream()
                                // 각 ListElement의 items를 평탄화
                                .flatMap(listElem -> Optional.ofNullable(listElem.getItems())
                                        .orElse(List.of())
                                        .stream())
                                .map(LibraryApiDto.Item::getRecomIsbn)
                                .filter(isbn -> isbn != null && !isbn.isBlank())
                                .collect(Collectors.toList());

                        return Mono.just(isbnList);
                    } catch (JAXBException e) {
                        e.printStackTrace();
                        return Mono.error(e);
                    }
                });
    }

    private LibraryApiDto parseXml(String xmlResponse) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(LibraryApiDto.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (LibraryApiDto) unmarshaller.unmarshal(new StringReader(xmlResponse));
    }
}
