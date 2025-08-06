package com.resumeanalyzer.config;

import com.resumeanalyzer.service.CustomUserDetailsService;
import com.resumeanalyzer.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Skip filtering for public endpoints
        return path.startsWith("/api/auth") ||
               path.equals("/") ||
               path.equals("/index.html") ||
               path.equals("/login") ||
               path.equals("/login.html") ||
               path.startsWith("/styles.css") ||
               path.startsWith("/script.js") ||
               path.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // Check if Authorization header exists and has Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (Exception e) {
                System.out.println("Invalid JWT token: " + e.getMessage());
                // Continue filter chain without setting authentication for invalid tokens
                filterChain.doFilter(request, response);
                return;
            }
        } else if (authHeader != null) {
            // Authorization header exists but doesn't start with "Bearer "
            System.out.println("Invalid Authorization header format");
            filterChain.doFilter(request, response);
            return;
        }

        // If we have a username and no authentication is set yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Validate token
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    List<String> roles = jwtUtil.extractAuthorities(jwtToken);

                    // Debug log
                    System.out.println("Extracted Roles from JWT: " + roles);

                    List<GrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Debug
                    System.out.println("Authentication set for user: " + username + " with authorities: " + authorities);
                }
            } catch (Exception e) {
                System.out.println("Error validating token for user: " + username + ", " + e.getMessage());
                // Continue without setting authentication
            }
        } else if (username == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            // Token exists but username couldn't be extracted
            System.out.println("Token exists but username couldn't be extracted");
        }

        filterChain.doFilter(request, response);
    }
}
