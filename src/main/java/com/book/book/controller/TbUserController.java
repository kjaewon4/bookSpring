package com.book.book.controller;


import com.book.book.dto.BookDto;
import com.book.book.dto.LoginRequestDto;
import com.book.book.dto.MyPageResponseDto;
import com.book.book.entity.TbBook;
import com.book.book.entity.TbBookmark;
import com.book.book.entity.TbUser;
import com.book.book.jwt.JwtUtil;
import com.book.book.repository.TbBookmarkRepository;
import com.book.book.repository.TbUserRepository;
import com.book.book.service.TbBookService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class TbUserController {
    private final TbUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TbUserRepository tbUserRepository;
    private final TbBookmarkRepository tbBookmarkRepository;
    private final TbBookService tbBookService;

    @Operation(summary = "로그인", description = "로그인")
    @ResponseBody
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        // 아이디/비밀번호로 인증 객체 생성
        var authToken = new UsernamePasswordAuthenticationToken(
                request.getUserUuid(), request.getUserPassword());

        try {
            // 아이디/비밀번호를 DB와 비교하여 로그인
            var auth = authenticationManagerBuilder.getObject().authenticate(authToken);

            // 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(auth);

            var jwt = JwtUtil.createToken(SecurityContextHolder.getContext().getAuthentication());
            System.out.println("Generated JWT: " + jwt);

            // JWT를 쿠키에 저장
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true); // JavaScript에서 접근하지 못하도록
            jwtCookie.setPath("/"); // 모든 경로에서 접근 가능
            jwtCookie.setSecure(false);  // HTTP에서도 전송되도록
            jwtCookie.setMaxAge(60 * 60 * 24); // 쿠키 만료 시간 (1일)
            response.addCookie(jwtCookie);

            response.setHeader("Set-Cookie", "jwt=" + jwt + "; Path=/; HttpOnly; SameSite=Lax; Secure=false");

            // 세션 ID를 반환하는 예시
            // 로그인 성공 시 JWT와 사용자 UUID를 반환
            return ResponseEntity.ok(Map.of(
                    "message", "로그인 성공",
                    "token", jwt,
                    "userUuid", request.getUserUuid()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "아이디 또는 비밀번호가 잘못되었습니다."));
        }
    }

    @Operation(summary = "로그아웃", description = "현재 사용자의 세션을 종료합니다.")
    @PostMapping("/logout") // ✅ GET → POST 변경
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        // JWT 쿠키 삭제 (HttpOnly라서 JS에서 삭제 불가능 → 서버에서 직접 만료 처리)
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }


    @Operation(summary = "회원가입", description = "회원가입")
    @PostMapping("/signup")
    public String signup(@RequestBody TbUser tbUser) {
        TbUser User = new TbUser();
        var hashPassword = passwordEncoder.encode((tbUser.getUserPassword()));

        tbUser.setUserUuid(tbUser.getUserUuid());
        tbUser.setUserPassword(hashPassword);

        userRepository.save(tbUser);

        return "회원가입이 완료되었습니다.";
    }

    @Operation(summary = "로그인 상태 확인", description = "현재 사용자가 로그인했는지 확인합니다.")
    @PostMapping("/status")
    public ResponseEntity<?> checkLoginStatus(
            @CookieValue(name = "jwt", required = false) String jwtToken
    ) {
        // JWT 쿠키가 없다면 로그인되지 않은 상태
        if (jwtToken == null || jwtToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인되어 있지 않습니다."));
        }

        // 토큰에서 사용자 UUID 추출
        String userUuid;
        try {
            userUuid = JwtUtil.getUserUuidFromToken(jwtToken);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "유효하지 않은 토큰입니다."));
        }

        // 실제 사용자 존재 여부 확인 (DB 기준)
        Optional<TbUser> userOpt = tbUserRepository.findByUserUuid(userUuid);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "존재하지 않는 사용자입니다."));
        }

        // 로그인된 사용자 정보 응답
        return ResponseEntity.ok(Map.of(
                "message", "로그인 상태입니다.",
                "userUuid", userUuid
        ));
    }

//    @GetMapping("/mypage")
//    public ResponseEntity<?> mypage(
//            Authentication authentication
//
//    ) {
//        // JWT에서 인증된 사용자 정보에서 userUuid 추출
//        String userUuid = (String) authentication.getPrincipal();
//        // userUuid로 사용자 정보를 조회
//        Optional<TbUser> userOpt = tbUserRepository.findByUserUuid(userUuid);
//        if (!userOpt.isPresent()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사용자를 찾을 수 없습니다.");
//        }
//
//        TbUser user = userOpt.get();
//        System.out.println("mypage user: " + user);
//
//        // 북마크 정보 보내기
//        Long userId = user.getUserId();
//
//        List<TbBookmark> bookmarkedBooks = tbBookmarkRepository.findAllByUserUserId(userId);
//        // TbBookmark에서 TbBook 정보를 추출하고, BookDto로 변환 (중복 제거)
//        List<TbBook> books = bookmarkedBooks.stream()
//                .map(TbBookmark::getBook)
//                .distinct()
//                .collect(Collectors.toList());
//
//        // TbBook 리스트를 BookDto 리스트로 변환
//        List<BookDto> bookDtoList = tbBookService.getBookDto(books);
//
//        // DTO 생성: 사용자 정보와 BookDto 리스트를 통합
//        MyPageResponseDto responseDto = new MyPageResponseDto(user, bookDtoList);
//
//        // JSON으로 변환되어 클라이언트에 전달됨 (Spring Boot의 자동 변환 기능 활용)
//        return ResponseEntity.ok(responseDto);
//
//    }


}
