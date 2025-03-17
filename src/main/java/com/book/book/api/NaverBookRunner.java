////package com.book.book.api;
////
//
//package com.book.book.api;
//
//import com.book.book.dto.NaverApiDto;
//import com.book.book.entity.TbBook;
//import com.book.book.repository.TbBookRepository;
//import com.book.book.service.LibraryApiService;
//import com.book.book.service.NaverBookService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//public class NaverBookRunner implements CommandLineRunner {
//
//    private final LibraryApiService libraryApiService;
//    private final NaverBookService naverBookService;
//    private final TbBookRepository tbBookRepository;
//
//    @Autowired
//    public NaverBookRunner(NaverBookService naverBookService, TbBookRepository tbBookRepository, LibraryApiService libraryApiService) {
//        this.naverBookService = naverBookService;
//        this.tbBookRepository = tbBookRepository;
//        this.libraryApiService = libraryApiService;
//    }
//
//    @Override
//    public void run(String... args) {
//        // Library API에서 recomisbn 목록을 동기적으로 가져옴
//        List<String> recomisbnList = libraryApiService.getRecomisbn().block();
//
//        // 각 ISBN이 13자 이상이면 13자리로 자르기
//        List<String> trimmedIsbnList = recomisbnList.stream()
//                .map(isbn -> (isbn != null && isbn.length() > 13) ? isbn.substring(0, 13) : isbn)
//                .collect(Collectors.toList());
//
//        if (trimmedIsbnList != null && !trimmedIsbnList.isEmpty()) {
//            for (String recomisbn : trimmedIsbnList) {
//                // 각 recomisbn을 이용해 Naver API에서 책 정보를 가져옴
//                NaverApiDto naverApiDto = naverBookService.searchBooks(recomisbn).block();
//
//                if (naverApiDto != null
//                        && naverApiDto.getChannel() != null
//                        && naverApiDto.getChannel().getItems() != null) {
//
//                    // Naver API 결과에서 책 정보를 순회하며 TbBook 엔티티 생성 후 저장
//                    naverApiDto.getChannel().getItems().forEach(naverBook -> {
//                        TbBook book = new TbBook(
//                                naverBook.getTitle(),
//                                naverBook.getImage(),
//                                naverBook.getAuthor(),
//                                naverBook.getPublisher(),
//                                naverBook.getIsbn(),
//                                naverBook.getDescription()
//                        );
//
//                        tbBookRepository.save(book);
//                        System.out.println("저장된 책: [" + recomisbn + "] - " + book.getBookTitle());
//                    });
//                } else {
//                    System.out.println("Naver API에서 검색된 결과가 없습니다: " + recomisbn);
//                }
//            }
//        } else {
//            System.err.println("Library API에서 검색된 결과가 없습니다.");
//        }
//    }
//}
//
////import com.book.book.dto.LibraryApiDto;
////import com.book.book.dto.NaverApiDto;
////import com.book.book.entity.TbBook;
////import com.book.book.repository.TbBookRepository;
////import com.book.book.service.LibraryApiService;
////import com.book.book.service.NaverBookService;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.boot.CommandLineRunner;
////import org.springframework.stereotype.Component;
////import reactor.core.publisher.Mono;
////
////import java.util.List;
////
////@Component
////public class NaverBookRunner implements CommandLineRunner {
////    private final LibraryApiService libraryApiService;
////    private final NaverBookService naverBookService;
////    private final TbBookRepository tbBookRepository;
////
////    @Autowired
////    public NaverBookRunner(NaverBookService naverBookService, TbBookRepository tbBookRepository, LibraryApiService libraryApiService) {
////        this.naverBookService = naverBookService;
////        this.tbBookRepository = tbBookRepository;
////        this.libraryApiService = libraryApiService;
////    }
////
////    @Override
////    public void run(String... args) {
////        // 검색어 리스트
////        // recomisbnList 각각 13자리씩 끊기
////        List<String> recomisbnList = libraryApiService.getRecomisbn().block(); // 동기적으로 결과 기다림
////
////        if (recomisbnList != null) {
////            // 각 recomisbn을 NaverBookService로 전달하여 책 정보를 가져오기
////            for (String recomisbn : recomisbnList) {
////                // Mono에서 동기적으로 값 받기, Mono에 대해 block()을 호출해야 함
////                NaverApiDto naverApiDto = naverBookService.searchBooks(recomisbn).block(); // block() 사용하여 동기적으로 결과를 받음
////
////                if (naverApiDto != null && naverApiDto.getChannel() != null && naverApiDto.getChannel().getItems() != null) {
////                    // NaverApiDto에서 책 정보 처리
////                    naverApiDto.getChannel().getItems().forEach(naverBook -> {
////                        TbBook book = new TbBook(
////                                naverBook.getTitle(),
////                                naverBook.getImage(),
////                                naverBook.getAuthor(),
////                                naverBook.getPublisher(),
////                                naverBook.getIsbn(),
////                                naverBook.getDescription()
////                        );
////
////                        // 책 정보를 DB에 저장
////                        tbBookRepository.save(book);
////                        System.out.println("저장된 책: [" + recomisbn + "] - " + book.getBookTitle());
////                    });
////                } else {
////                    System.out.println("Naver API에서 검색된 결과가 없습니다: " + recomisbn);
////                }
////            }
////        } else {
////            System.err.println("Library API에서 검색된 결과가 없습니다.");
////        }
////    }
////}
////
////
////
////
////
