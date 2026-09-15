package com.example.selfblog.config;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpStatus;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginRateLimitFilter extends OncePerRequestFilter {
    private final LoginAttemptService loginAttemptService;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    public LoginRateLimitFilter(LoginAttemptService loginAttemptService, MessageSource messageSource,
            LocaleResolver localeResolver) {
        this.loginAttemptService = loginAttemptService;
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (loginAttemptService.isBlocked(request.getRemoteAddr())) {
            String message = messageSource.getMessage("auth.blocked", null, localeResolver.resolveLocale(request));
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\":\"" + message + "\"}");
            } else {
                request.getSession().setAttribute("LOGIN_ERROR", message);
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return;
        }
        filterChain.doFilter(request, response);
    }
}
