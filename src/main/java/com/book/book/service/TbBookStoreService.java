package com.book.book.service;

import com.book.book.dto.TbBookStoreResponseDto;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.StringReader;

@Service
@RequiredArgsConstructor
public class TbBookStoreService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ttb.key}")
    private String ttbKey;

    public Mono<TbBookStoreResponseDto> fetchBookStores(String isbn) {
        String url = "http://www.aladin.co.kr/ttb/api/ItemOffStoreList.aspx"
                + "?TTBKey=" + ttbKey
                + "&itemIdType=ISBN13"
                + "&ItemId=" + isbn
                + "&output=xml";

        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(xmlResponse -> {
                    if (xmlResponse != null) {
                        System.out.println("API 응답 XML:");
                        System.out.println(xmlResponse);
                        try {
                            System.out.println("Mono.just(parseXml(xmlResponse)) : "+Mono.just(parseXml(xmlResponse)));
                            return Mono.just(parseXml(xmlResponse));
                        } catch (JAXBException e) {
                            return Mono.error(e);
                        }
                    }
                    return Mono.empty();
                });
    }

    // JAXB를 이용해 XML을 DTO로 변환
    private TbBookStoreResponseDto parseXml(String xmlResponse) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(TbBookStoreResponseDto.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        // XML 파싱 전 로그 출력
        System.out.println("Parsing XML: " + xmlResponse);
        return (TbBookStoreResponseDto) unmarshaller.unmarshal(new StringReader(xmlResponse));
    }
}

//
//@Service
//@RequiredArgsConstructor
//public class TbBookStoreService {
//
//    private final WebClient.Builder webClientBuilder;
//
//    private final TbBookStoreRepository tbBookStoreRepository;
//
//    @Value("${ttb.key}")
//    private String ttbKey;
//
//    public Mono<Void> fetchAndSaveData(String itemId) {
//        String url = "http://www.aladin.co.kr/ttb/api/ItemOffStoreList.aspx"
//                + "?TTBKey=" + ttbKey
//                + "&itemIdType=ISBN13"  // TODO : 10자리 13자리 분기
//                + "&ItemId=" + itemId
//                + "&output=xml";
//
//        // WebClient를 사용하여 API 호출
//        return webClientBuilder.baseUrl(url)
//                .build()
//                .get()
//                .retrieve()
//                .bodyToMono(String.class)
//                .flatMap(xmlResponse -> {
//                    if (xmlResponse != null) {
//                        System.out.println("API 응답 XML:");
//                        System.out.println(xmlResponse);
//                        try {
//                            TbBookStoreResponseDto apiResponse = parseXml(xmlResponse);
//
//                            // 매장 정보 저장
//                            if (apiResponse.getItemOffStoreList() != null) {
//                                for (TbBookStoreDto store : apiResponse.getItemOffStoreList()) {
//                                    TbBookStore tbBookStore = new TbBookStore();
//                                    tbBookStore.setOffName(store.getOffName());
//                                    tbBookStore.setLink(store.getLink());
//                                    tbBookStoreRepository.save(tbBookStore);
//                                }
//                            }
//                        } catch (JAXBException e) {
//                            return Mono.error(e);
//                        }
//                    }
//                    return Mono.empty();
//                });
//    }
//
//    // JAXB를 이용해 XML을 DTO로 변환
//    private TbBookStoreResponseDto parseXml(String xmlResponse) throws JAXBException {
//        JAXBContext context = JAXBContext.newInstance(TbBookStoreResponseDto.class);  //  TbBookStoreResponseDto 클래스를 XML과 매핑하는 컨텍스트를 생성
//        Unmarshaller unmarshaller = context.createUnmarshaller();  // XML 데이터를 TbBookStoreResponseDto 객체로 변환
//        StringReader reader = new StringReader(xmlResponse);  // StringReader: XML 데이터를 문자 스트림으로 읽을 수 있게 해주는 클래스. xmlResponse라는 XML 문자열을 StringReader로 감싸서 읽을 수 있게 함
//
//        return (TbBookStoreResponseDto) unmarshaller.unmarshal(reader);
//    }
//}
