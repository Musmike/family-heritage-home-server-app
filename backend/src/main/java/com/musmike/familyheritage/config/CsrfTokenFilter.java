// /home/musmike/Documents/projects/github_projects/family-heritage-home-server-app/backend/src/main/java/com/musmike/familyheritage/config/CsrfTokenFilter.java
package com.musmike.familyheritage.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class CsrfTokenFilter extends OncePerRequestFilter {

    private static final String XSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";
    private static final String XSRF_TOKEN_HEADER_NAME = "X-XSRF-TOKEN";
    private static final Set<String> ALLOWED_METHODS = new HashSet<>(
            Arrays.asList(HttpMethod.GET.name(), HttpMethod.HEAD.name(), HttpMethod.OPTIONS.name(), HttpMethod.TRACE.name())
    );

    // Lista ścieżek, które mają być ignorowane przez filtr CSRF
    private final RequestMatcher authEndpointsMatcher = new AntPathRequestMatcher("/auth/**");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Jeśli żądanie pasuje do ścieżek /api/auth/**, pomiń filtr
        if (authEndpointsMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (ALLOWED_METHODS.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String headerToken = request.getHeader(XSRF_TOKEN_HEADER_NAME);
        String cookieToken = null;

        if (request.getCookies() != null) {
            cookieToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> XSRF_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (headerToken == null || cookieToken == null || !headerToken.equals(cookieToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}