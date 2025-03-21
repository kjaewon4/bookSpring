package com.book.book.controller;

import com.book.book.dto.BookDto;
import com.book.book.entity.TbBook;
import com.book.book.entity.TbBookmark;
import com.book.book.entity.TbUser;
import com.book.book.repository.TbBookRepository;
import com.book.book.repository.TbBookmarkRepository;
import com.book.book.repository.TbUserRepository;
import com.book.book.service.TbBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/bookmarks")
public class TbBookmarkController {
    private final TbUserRepository tbUserRepository;
    private final TbBookRepository tbBookRepository;
    private final TbBookmarkRepository tbBookmarkRepository;
    private final TbBookService tbBookService;

    @Operation(
            summary = "ISBN으로 북마크 추가",
            description = "사용자가 특정 ISBN의 책을 북마크에 추가합니다. 요청 본문에 회원 UUID가 필요합니다.",
            responses = {
        @ApiResponse(responseCode = "200", description = "북마크 추가 성공"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요함"),
    })
    @PostMapping("{isbn}")
    public ResponseEntity<?> addBookMark(
            @Parameter(description = "북마크할 도서의 ISBN 번호", example = "9788920930720")
            @PathVariable("isbn") String isbn,
            Authentication authentication
    ) {

        // SecurityContextHolder에 저장된 principal을 가져옵니다.
        String userUuid = (String) authentication.getPrincipal();

        System.out.println("addBookMark userUuid: " + userUuid);

        if (userUuid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        TbBook book = tbBookRepository.findByBookIsbn(isbn)
                .orElseThrow(() -> new RuntimeException(isbn + "에 해당하는 도서를 찾을 수 없습니다."));

        Optional<TbUser> user = tbUserRepository.findByUserUuid(userUuid);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("해당 사용자가 존재하지 않습니다.");
        }
        System.out.println("addBookMark user.get(): " + user.get());


        Long userId = user.get().getUserId(); // get()을 통해 TbUser 객체를 꺼내고 그 객체의 getUserId()를 호출
        // user_id를 기반으로 TbBookmark를 조회
        List<TbBookmark> bookmarkedBooks = tbBookmarkRepository.findAllByUserUserId(userId);


        // 중복 체크: 이미 해당 isbn 책이 북마크되어 있는지 확인
        Optional<TbBookmark> existingBookmark = tbBookmarkRepository.findByBookBookIsbnAndUserUserId(isbn, userId);
        if (existingBookmark.isPresent()) {
            // 이미 북마크된 책이 있으면 중복 처리
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 북마크된 책입니다.");
        }

        TbBookmark bookmark = new TbBookmark();
        bookmark.setBook(book);
        bookmark.setUser(user.get());
        tbBookmarkRepository.save(bookmark);

        System.out.println("addBookMark user: " + bookmark);

        return ResponseEntity.ok("북마크에 추가되었습니다.");
    }

    // 북마크 리스트
    @Operation(
            summary = "회원별 북마크 조회",
            description = "회원의 UUID를 기반으로 해당 회원이 추가한 북마크 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 북마크 목록 반환"),
                    @ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없음")
            }
    )
    @GetMapping("")
    public ResponseEntity<?> getBookmarks(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,   // 기본 0페이지 (첫 번째 페이지)
            @RequestParam(defaultValue = "20") int size    // 한 페이지당 20개
    ) {
        // JWT에서 인증된 사용자 정보에서 userUuid 추출
        String userUuid = (String) authentication.getPrincipal();

        // uuid로 사용자의 북마크 목록을 가져오는 서비스 호출
        System.out.println("getBookmarks uuid: " + userUuid);

        // userUuid로 사용자 정보를 조회
        Optional<TbUser> userOpt = tbUserRepository.findByUserUuid(userUuid);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자를 찾을 수 없습니다.");
        }

        Long userId = userOpt.get().getUserId();

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size);

        // user_id를 기반으로 TbBookmark를 페이지 단위로 조회
        Page<TbBookmark> bookmarkedBooksPage = tbBookmarkRepository.findAllByUserUserId(userId, pageable);
        System.out.println("bookmarkedBooksPage: " + bookmarkedBooksPage);

        if (bookmarkedBooksPage.isEmpty()) {
            // 페이지 정보 포함 빈 리스트 응답 (또는 필요에 따라 다른 구조로 반환)
            return ResponseEntity.ok(Collections.emptyMap());
        }

        // TbBookmark에서 TbBook 정보를 추출하고, BookDto로 변환 (중복 제거)
        List<TbBook> books = bookmarkedBooksPage.stream()
                .map(TbBookmark::getBook)
                .distinct()
                .collect(Collectors.toList());

        List<BookDto> bookDtos = tbBookService.getBookDto(books);

        // 페이징 정보를 포함한 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("content", bookDtos);
        response.put("currentPage", bookmarkedBooksPage.getNumber());
        response.put("totalPages", bookmarkedBooksPage.getTotalPages());
        response.put("totalElements", bookmarkedBooksPage.getTotalElements());
        response.put("numberOfElements", bookmarkedBooksPage.getNumberOfElements());

        return ResponseEntity.ok(response);
    }



    // 북마크 삭제
    @Operation(
            summary = "회원별 북마크 삭제",
            description = "회원이 추가한 북마크 중 특정 ISBN의 도서를 삭제합니다. 요청 본문에 회원 UUID가 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "북마크 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인이 필요함"),
                    @ApiResponse(responseCode = "404", description = "삭제할 북마크가 존재하지 않음")
            }
    )
    @Transactional
    @DeleteMapping("{isbn}")
    public ResponseEntity<?> deleteBookMark(
            @Parameter(description = "삭제할 도서의 ISBN 번호", example = "9788920930720")
            @PathVariable("isbn") String isbn,
            Authentication authentication) {

        // JWT에서 인증된 사용자 정보에서 userUuid 추출
        String userUuid = (String) authentication.getPrincipal();
        if(userUuid == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        }

        TbBook book = tbBookRepository.findByBookIsbn(isbn)
                .orElseThrow(() -> new RuntimeException(isbn + "에 해당하는 도서를 찾을 수 없습니다."));

        // uuid로 유저 정보 가져옴
        Optional<TbUser> user = tbUserRepository.findByUserUuid(userUuid);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("해당 사용자가 존재하지 않습니다.");
        }

        Long userId = user.get().getUserId(); // get()으로 TbUser 객체 꺼내고 그 객체의 getUserId()호출

        // 북마크 존재 여부 확인 후 삭제
        Optional<TbBookmark> existingBookmark = tbBookmarkRepository.findByBookBookIsbnAndUserUserId(isbn, userId);
        if (existingBookmark.isPresent()) {
            tbBookmarkRepository.delete(existingBookmark.get());
            return ResponseEntity.ok("북마크가 삭제되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("북마크가 존재하지 않습니다.");
        }

    }


}
