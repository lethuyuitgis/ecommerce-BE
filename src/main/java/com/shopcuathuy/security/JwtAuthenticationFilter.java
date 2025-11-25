package com.shopcuathuy.security;

import com.shopcuathuy.entity.User;
import com.shopcuathuy.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);
        HttpServletRequest requestToUse = request;

        if (StringUtils.hasText(token)) {
            if (tokenProvider.validateToken(token)) {
                String userId = tokenProvider.getUserId(token);
                Optional<User> userOpt = userRepository.findById(userId);

                if (userOpt.isPresent() && userOpt.get().getStatus() == User.UserStatus.ACTIVE) {
                    User user = userOpt.get();
                    List<GrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getUserType().name())
                    );

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    if (request.getHeader("X-User-Id") == null) {
                        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
                        mutableRequest.putHeader("X-User-Id", user.getId());
                        mutableRequest.putHeader("X-User-Role", user.getUserType().name());
                        requestToUse = mutableRequest;
                    }
                } else {
                    // User not found or inactive - clear any existing authentication
                    SecurityContextHolder.clearContext();
                }
            } else {
                // Invalid token - clear any existing authentication
                SecurityContextHolder.clearContext();
            }
        }
        // If no token, continue without authentication (will be handled by SecurityConfig rules)

        filterChain.doFilter(requestToUse, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}


