package com.book.book.controller;


import com.book.book.dto.LoginRequestDto;
import com.book.book.entity.TbUser;
import com.book.book.jwt.JwtUtil;
import com.book.book.repository.TbUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
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

        return "";
    }

}
