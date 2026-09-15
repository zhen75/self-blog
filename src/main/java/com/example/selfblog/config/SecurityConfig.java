package com.example.selfblog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.MessageSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, LoginAttemptService loginAttemptService,
            AuditLogService auditLogService, MessageSource messageSource, LocaleResolver localeResolver) throws Exception{
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/login").permitAll()
            .requestMatchers(HttpMethod.GET,"/", "/articles/**", "/api/articles/**").permitAll()
            .anyRequest().hasRole("ADMIN")
        )
        .formLogin(form -> form
            .loginPage("/login")
            .successHandler((request, response, authentication) -> {
                loginAttemptService.recordSuccess(request.getRemoteAddr());
                auditLogService.authentication("success", authentication.getName(), request.getRemoteAddr());
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"redirect\":\"/admin\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin");
                }
            })
            .failureHandler((request, response, exception) -> {
                LoginAttemptService.FailureResult result = loginAttemptService.recordFailure(request.getRemoteAddr());
                auditLogService.authentication(result.blocked() ? "blocked" : "failure",
                        request.getParameter("username"), request.getRemoteAddr());
                String key = result.blocked() ? "auth.blocked" : "auth.invalid";
                String message = messageSource.getMessage(key, null, localeResolver.resolveLocale(request));
                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                    response.setStatus(result.blocked() ? 429 : 401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"" + message + "\"}");
                } else {
                    request.getSession().setAttribute("LOGIN_ERROR", message);
                    response.sendRedirect(request.getContextPath() + "/login");
                }
            })
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/?logout=true")
            .permitAll()
        )
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; img-src 'self' https: data:; style-src 'self'; "
                    + "script-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'"))
            .referrerPolicy(referrer -> referrer.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            .permissionsPolicyHeader(policy -> policy.policy(
                    "camera=(), microphone=(), geolocation=(), payment=()"))
        )
        .addFilterBefore(new LoginRateLimitFilter(loginAttemptService, messageSource, localeResolver),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder,
            @Value("${blog.admin.username}") String username,
            @Value("${blog.admin.password}") String password) {
        if (password == null || password.length() < 12) {
            throw new IllegalStateException("BLOG_ADMIN_PASSWORD must contain at least 12 characters");
        }
        UserDetails admin = User.builder()
        .username(username)
        .password(encoder.encode(password))
        .roles("ADMIN")
        .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
