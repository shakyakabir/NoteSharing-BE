package com.example.notesharing.config;

import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.User;
import com.example.notesharing.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = null;

        // 🔥 GET TOKEN FROM COOKIE
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }

        // ❌ NO TOKEN → BLOCK
//        if (token == null) {
//            filterChain.doFilter(request, response);
//            return;
//        }
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 🔥 VALIDATE TOKEN
            String email = jwtService.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Look up the user's role from the DB so admin access reflects the *current* role
                // immediately - no new JWT claim, no re-login, no change to token issuance. Defaults
                // to ROLE_USER when the user (or role) is missing, preserving the previous behaviour
                // for every non-admin request.
                String role = userRepository.findByEmail(email)
                        .map(User::getRole)
                        .map(Enum::name)
                        .orElse("USER");

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            // invalid token → ignore authentication
        }

        filterChain.doFilter(request, response);


    }




}
