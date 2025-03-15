package com.book.book.controller;

import com.book.book.dto.CustomUser;
import com.book.book.entity.TbUser;
import com.book.book.jwt.JwtUtil;
import com.book.book.repository.TbUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")  // CORS 직접 지정
@RestController
@RequiredArgsConstructor
public class TbUserController {
    private final TbUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Operation(summary = "로그인", description = "로그인")
    @ResponseBody
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> data, HttpServletResponse response, HttpSession session) {
        // 아이디/비밀번호로 인증 객체 생성
        var authToken = new UsernamePasswordAuthenticationToken(
                data.get("userUuid"), data.get("userPassword"));

        try {
            // 아이디/비밀번호를 DB와 비교하여 로그인
            var auth = authenticationManagerBuilder.getObject().authenticate(authToken);

            // 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(auth);

            var jwt = JwtUtil.createToken(SecurityContextHolder.getContext().getAuthentication());
            System.out.println("Generated JWT: " + jwt);

//            // **세션에 사용자 정보 저장**
//            session.setAttribute("userUuid", data.get("userUuid")); // UUID 저장
//            session.setAttribute("jwt", jwt); // JWT 저장
//            session.setMaxInactiveInterval(60 * 60 * 24); // 세션 만료 시간 (1일)

            // JWT와 userUuid를 쿠키에 저장
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true); // JavaScript에서 접근하지 못하도록
            jwtCookie.setPath("/"); // 모든 경로에서 접근 가능
            jwtCookie.setSecure(false);  // HTTP에서도 전송되도록
            jwtCookie.setMaxAge(60 * 60 * 24); // 쿠키 만료 시간 (1일)
            response.addCookie(jwtCookie);

//            Cookie userUuidCookie = new Cookie("userUuid", data.get("userUuid"));
//            userUuidCookie.setHttpOnly(true);
//            userUuidCookie.setPath("/");
//            userUuidCookie.setSecure(false);  // HTTP에서도 전송되도록
//            userUuidCookie.setMaxAge(60 * 60 * 24); // 쿠키 만료 시간 (1일)
//            response.addCookie(userUuidCookie);
//            response.setHeader("Set-Cookie", "userUuid=" + data.get("userUuid") + "; Path=/; HttpOnly; SameSite=Lax; Secure=false");
            response.setHeader("Set-Cookie", "jwt=" + jwt + "; Path=/; HttpOnly; SameSite=Lax; Secure=false");



            // 세션 ID를 반환하는 예시
            // 로그인 성공 시 JWT와 사용자 UUID를 반환
            return ResponseEntity.ok(Map.of(
                    "message", "로그인 성공",
                    "token", jwt,
                    "userUuid", data.get("userUuid")
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


    @Operation(summary = "세션 확인", description = "현재 로그인한 사용자 정보를 반환합니다.")
    @GetMapping("/session")
    public ResponseEntity<Map<String, String>> getSessionInfo(HttpSession session) {
        String userUuid = (String) session.getAttribute("userUuid");
        String jwt = (String) session.getAttribute("jwt");

        if (userUuid == null || jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "세션이 만료되었거나 로그인되지 않았습니다."));
        }

        return ResponseEntity.ok(Map.of(
                "message", "세션 정보 조회 성공",
                "userUuid", userUuid,
                "token", jwt
        ));
    }

//    // 서버 코드: 세션 상태를 확인하는 API
//    @ResponseBody
//    @GetMapping("/check-session")
//    public ResponseEntity<Map<String, Object>> checkSession(HttpSession session) {
//        // 세션에 저장된 사용자 정보 확인
//        Object authenticatedUser = session.getAttribute("authenticatedUser");
//
//        if (authenticatedUser != null) {
//            return ResponseEntity.ok(Map.of("user", authenticatedUser));
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(Map.of("error", "로그인 해주세요."));
//        }
//    }


    // JWT 로그인
//    @ResponseBody
//    @PostMapping("/login")
//    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> data
//            , HttpServletResponse response) {
//        // 아이디/비밀번호로 인증 객체 생성
//        var authToken = new UsernamePasswordAuthenticationToken(
//                data.get("userUuid"), data.get("userPassword"));
//
//        try {
//            // 아이디/비밀번호를 DB와 비교하여 로그인
//            var auth = authenticationManagerBuilder.getObject().authenticate(authToken);
//
//            // 인증 정보 저장
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            // JWT 생성
//            var jwt = JwtUtil.createToken(SecurityContextHolder.getContext().getAuthentication());
//            System.out.println("Generated JWT: " + jwt);
//
//            if (jwt == null || jwt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(Map.of("error", "토큰 생성 실패"));
//            }
//
//
//
//            // JWT를 HttpOnly 쿠키에 저장
//            Cookie cookie = new Cookie("jwt", jwt);
//            cookie.setHttpOnly(true);  // 클라이언트 자바스크립트에서 접근 불가
//            cookie.setPath("/");  // 쿠키가 유효한 경로 설정
//            cookie.setMaxAge(60 * 60);  // 1시간 동안 유효
//            cookie.setDomain("localhost");
//            response.setHeader("Set-Cookie",
//                    "jwt=" + jwt +
//                            "; Path=/; HttpOnly; SameSite=None");// 쿠키가 다른 도메인에서도 전송되도록 설정
//            response.addCookie(cookie);
//
//
//            // JWT를 JSON 형식으로 반환
//            return ResponseEntity.ok(Map.of("jwt", jwt));
//
//        } catch (AuthenticationException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(Map.of("error", "아이디 또는 비밀번호가 잘못되었습니다."));
//        }
//    }

    // 세션 로그인
//    @PostMapping("/login")
//    public String login(@RequestBody TbUser tbUser) {
//        Optional<TbUser> result = userRepository.findByUserName("jw");
//
//        if (result.isPresent()) {
//            System.out.println(result.get());
//        } else {
//            System.out.println("사용자를 찾을 수 없습니다.");
//        }
//
//        return "";
//    }

    @Operation(summary = "회원가입", description = "회원가입")
    @PostMapping("/signup")
    public String signup(@RequestBody TbUser tbUser) {
        TbUser User = new TbUser();
        var hashPassword = passwordEncoder.encode((tbUser.getUserPassword()));

        tbUser.setUserUuid(tbUser.getUserUuid());
        tbUser.setUserPassword(hashPassword);

        userRepository.save(tbUser);

        return "";
    }

}
