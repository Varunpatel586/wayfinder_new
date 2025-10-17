package com.varun.wayfinder.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        // Allow access to static resources and auth pages without authentication
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") ||
                path.equals("/") || path.equals("/login") || path.equals("/register") || path.equals("/SignUp")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null && !token.trim().isEmpty()) {
            try {
                String username = jwtUtil.extractUsername(token);
                if (username != null && !username.isEmpty() && jwtUtil.validateToken(token, username)) {
                    // Token is valid, proceed with the request
                    request.setAttribute("username", username);
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    System.out.println("Token validation failed for user: " + username);
                }
            } catch (Exception e) {
                System.err.println("Error validating token: " + e.getMessage());
                // Clear invalid token
                Cookie cookie = new Cookie("token", "");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        // If we get here, the user is not authenticated
        response.sendRedirect("/");
    }
}

