package com.clipers.clipers.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, 
                                  CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        System.out.println("=== JWT FILTER PROCESSING ===");
        System.out.println("Processing: " + request.getMethod() + " " + request.getRequestURI());
        
        try {
            String jwt = getJwtFromRequest(request);
            System.out.println("JWT Token present: " + (jwt != null));

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String email = tokenProvider.getEmailFromToken(jwt);
                System.out.println("Valid JWT for email: " + email);

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Authentication set successfully");
            } else {
                System.out.println("No valid JWT token found");
            }
        } catch (Exception ex) {
            System.out.println("JWT Filter error: " + ex.getMessage());
            logger.error("Could not set user authentication in security context", ex);
        }

        System.out.println("=== JWT FILTER COMPLETE ===");
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("=== JWT FILTER CHECK ===");
        System.out.println("Path: " + path);
        System.out.println("Method: " + method);
        
        // Skip JWT filter for public endpoints only
        // /api/auth/me should NOT be skipped - it needs JWT authentication
        boolean shouldSkip = path.equals("/api/auth/login") ||
                            path.equals("/api/auth/register") ||
                            path.equals("/api/auth/refresh") ||
                            path.startsWith("/api/test/") ||
                            path.startsWith("/api/public/") ||
                            path.startsWith("/uploads/") ||
                            path.startsWith("/actuator/health") ||
                            path.equals("/error") ||
                            path.equals("/") ||
                            path.equals("/favicon.ico");
        
        System.out.println("Should skip JWT filter: " + shouldSkip);
        System.out.println("=========================");
        
        return shouldSkip;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
