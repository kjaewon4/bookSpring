//package com.book.book.api;
//
//import com.book.book.service.LibraryApiService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class LibraryApiRunner implements CommandLineRunner {
//
//    private final LibraryApiService libraryApiService;
//
//    @Autowired
//    public LibraryApiRunner(LibraryApiService libraryApiService) {
//        this.libraryApiService = libraryApiService;
//    }
//
//    @Override
//    public void run(String... args) {
//        libraryApiService.getRecomisbn()
//                .doOnNext(recomisbnList -> {
//                    if (!recomisbnList.isEmpty()) {
//                        System.out.println("📚 추천된 ISBN 목록:");
////                        recomisbnList.forEach(isbn -> System.out.println(" - " + isbn));
//                    } else {
//                        System.out.println("❌ 추천된 ISBN이 없습니다.");
//                    }
//                })
//                .doOnError(error -> System.err.println("🚨 API 호출 중 오류 발생: " + error.getMessage()))
//                .subscribe();
//    }
//}
