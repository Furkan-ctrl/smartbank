package com.smartbank.userservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try{
            String email = jwtUtil.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null){
                if (jwtUtil.isTokenValid(token, email) && "ACCESS".equals(jwtUtil.extractTokenType(token))){

                    String role = jwtUtil.extractRole(token);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email,
                            null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }


        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException e) {
            logger.debug("JWT validation failed: " + e.getMessage());

        } catch (Exception e) {
            logger.warn("Unexpected error during JWT authentication", e);
        }

        filterChain.doFilter(request, response);

    }
}
