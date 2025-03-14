package com.book.book.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AuthenticationService {

    // 쿠키에서 JWT와 userUuid 추출 후 검증
    public String validateJwtAndExtractUserUuid(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String jwt = null;
        String userUuid = null;

        System.out.println("Request cookies: " + Arrays.toString(cookies)); // 쿠키 확인

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                }
                if ("userUuid".equals(cookie.getName())) {
                    userUuid = cookie.getValue();
                }
            }
        }

        if (jwt == null || userUuid == null) {
            System.out.println("No JWT or userUuid found in cookies");  // 디버깅용 메시지
            return null;
        }

        // JWT 검증을 추가한다면 여기서 검증을 할 수 있습니다.
        return userUuid;  // 검증 성공 시 userUuid 반환
    }
}
