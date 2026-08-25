    //package com.example.notesharing.config;
    //
    //
    //import org.springframework.context.annotation.Bean;
    //import org.springframework.context.annotation.Configuration;
    //import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    //import org.springframework.security.config.http.SessionCreationPolicy;
    //import org.springframework.security.web.SecurityFilterChain;
    //@Configuration
    //public class SecurityConfig {
    //    @Bean
    //    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //
    //        http
    //                .cors(cors -> {})
    //                .csrf(csrf -> csrf.disable())
    //
    //                .authorizeHttpRequests(auth -> auth
    //                        .requestMatchers(
    //                                "/api/auth/**",
    //                                "/oauth2/**",
    //                                "/login/**"
    //                        ).permitAll()
    //                        .anyRequest().authenticated()
    //                )
    //
    //                .oauth2Login(oauth -> oauth
    //                        .successHandler((request, response, authentication) -> {
    //                            response.sendRedirect("http://localhost:3000/login-success");
    //                        })
    //                )
    //
    //                .sessionManagement(session -> session
    //                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    //                );
    //
    //        return http.build();
    //    }
    //}
    
    
    
    package com.example.notesharing.config;
    
    import com.example.notesharing.service.JwtService;
    import com.example.notesharing.Repository.UserRepository;
    import jakarta.servlet.http.Cookie;
    import jakarta.servlet.http.HttpServletResponse;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.oauth2.core.user.OAuth2User;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    
    @Configuration
    public class SecurityConfig {
    
        private final JwtService jwtService;
        private final UserRepository userRepository;

        public SecurityConfig(JwtService jwtService, UserRepository userRepository) {
            this.jwtService = jwtService;
            this.userRepository = userRepository;
        }
    
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    
            http
                    .cors(cors -> {})
                    .csrf(csrf -> csrf.disable())
                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint((request, response, authException) ->
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                            .accessDeniedHandler((request, response, accessDeniedException) ->
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN))
                    )
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/api/auth/**",
                                    "/api/register",
                                    "/api/verify-otp",
                                    "/oauth2/**",
                                    "/login/**",
                                    // 🔥 SWAGGER FIX
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/v3/api-docs/**",
                                    "/v3/api-docs.yaml",
                                    // eSewa
                                    "/api/subscriptions/payment/esewa/success",
                                    "/api/subscriptions/payment/esewa/failure"
                            ).permitAll()
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")
                            .anyRequest().authenticated()
                    )
    
                    // 🔥 GOOGLE LOGIN → RETURN JWT INSTEAD OF SESSION
                    .oauth2Login(oauth -> oauth
                            .successHandler((request, response, authentication) -> {
    
                                OAuth2User user = (OAuth2User) authentication.getPrincipal();
    
                                String email = user.getAttribute("email");
    
                                String accessToken = jwtService.generateToken(email);
                                String refreshToken = jwtService.generateRefreshToken(email);
    
                                Cookie accessCookie = new Cookie("accessToken", accessToken);
                                accessCookie.setHttpOnly(true);
                                accessCookie.setPath("/");
                                accessCookie.setMaxAge(60 * 60);
    
                                response.addCookie(accessCookie);
    
                                Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
                                refreshCookie.setHttpOnly(true);
                                refreshCookie.setPath("/");
                                refreshCookie.setMaxAge(7 * 24 * 60 * 60);
    
                                response.addCookie(refreshCookie);
    
                                response.sendRedirect("http://localhost:3000/login-success");
                            }))
    
                    // 🔥 NO SESSION (JWT ONLY SYSTEM)
                    .sessionManagement(session ->
                            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )
             .addFilterBefore(new JwtAuthFilter(jwtService, userRepository),
                    UsernamePasswordAuthenticationFilter.class);

    
            return http.build();
        }
    }
