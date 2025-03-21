package com.book.book.controller;


import com.book.book.dto.LoginRequestDto;
import com.book.book.dto.PasswordChangeRequestDto;
import com.book.book.entity.TbUser;
import com.book.book.jwt.JwtUtil;
import com.book.book.repository.TbUserRepository;
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
import java.util.Map;
import java.util.Optional;


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

        return "회원가입이 완료되었습니다.";
    }


    // 비밀번호 변경
    // TbUser 엔티티에 토큰 버전(tokenVersion)이나 비밀번호 변경 시간을 기록하는 필드를 추가하고, JWT 검증 시 해당 정보를 활용해야 합니다.
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 검증 후 새 비밀번호로 변경합니다. 변경 시 기존 토큰을 무효화합니다.")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody PasswordChangeRequestDto request,
            HttpServletResponse response
    ) {
        // JWT를 체크
        // 현재 비밀번호 검증
        // 리포지토리에서 비밀번호 업데이트
        // JWT 유효성 갱신
        // 비밀번호 변경시 JWT 토큰 버전 변경이나 다른 방법으로 JWT를 무효화하는 과정이 필요
        // TbUser 엔티티에 tokenVersion이라든지 비슷한 속성이 필요
        // 비밀번호 변경 시 기존 JWT 토큰이 만료되지 않도록, 토큰 무효화를 위한 구현이 필요. SecurityContextHolder에서 현재 사용자를 가져와 비밀번호 확인 후 업데이트하고, passwordEncoder로 새 비밀번호를 암호화한 후 userRepository로 저장

        // 현재 인증된 사용자 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "인증 정보가 없습니다."));
        }

        // 사용자 조회 (예: userUuid로 조회)
        Optional<TbUser> userOptional = userRepository.findByUserUuid(auth.getName());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }
        TbUser user = userOptional.get();

        // 현재 비밀번호가 일치하는지 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getUserPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "현재 비밀번호가 일치하지 않습니다."));
        }

        // 새 비밀번호 암호화 및 업데이트
        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setUserPassword(hashedNewPassword);

        // 선택사항: 기존 토큰 무효화를 위해 토큰 버전 업데이트 혹은 비밀번호 변경 시간 기록
        // 예: user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        // 비밀번호 변경 후 재인증 및 JWT 재발급 (선택사항)
        UsernamePasswordAuthenticationToken newAuthToken =
                new UsernamePasswordAuthenticationToken(user.getUserUuid(), request.getNewPassword());
        try {
            var newAuth = authenticationManagerBuilder.getObject().authenticate(newAuthToken);
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            var newJwt = JwtUtil.createToken(newAuth);

            // 새로운 JWT를 쿠키에 저장 (HttpOnly 설정)
            Cookie jwtCookie = new Cookie("jwt", newJwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setSecure(false);
            jwtCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(jwtCookie);
            response.setHeader("Set-Cookie", "jwt=" + newJwt + "; Path=/; HttpOnly; SameSite=Lax; Secure=false");

            return ResponseEntity.ok(Map.of("message", "비밀번호 변경 성공", "token", newJwt));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "토큰 재발급에 실패하였습니다."));
        }
    }
}
