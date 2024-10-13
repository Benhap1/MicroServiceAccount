package ru.skillbox.mc_account.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.skillbox.mc_account.security.CustomUserDetailsService;
import ru.skillbox.mc_account.security.JwtUtils;
import java.io.IOException;


@Component
public class AuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private final RestTemplate restTemplate;
    private final String AUTH_SERVICE_URL = "http://auth-service/api/v1/auth/validate";
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthFilter(RestTemplate restTemplate, CustomUserDetailsService userDetailsService, JwtUtils jwtUtils) {
        this.restTemplate = restTemplate;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authToken = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        String requestURI = httpRequest.getRequestURI();

        if (isSwaggerRequest(requestURI)) {
            logger.debug("Swagger request: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        if (authToken != null && authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7);  // Убираем "Bearer " из токена
        }

        if (authToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (isValidToken(authToken, (HttpServletResponse) response)) {
                try {
                    String userEmail = jwtUtils.getEmailFromToken(authToken);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    logger.info("Authenticated user: {}", userEmail);
                } catch (Exception e) {
                    logger.warn("Authentication failed: {}", e.getMessage());
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isSwaggerRequest(String requestURI) {
        return requestURI.startsWith("/swagger") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/swagger-ui");
    }

    private boolean isValidToken(String token, HttpServletResponse response) throws IOException {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Authorization token is missing");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization token is missing");
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Void> authResponse = restTemplate.exchange(AUTH_SERVICE_URL, HttpMethod.GET, entity, Void.class);

            if (authResponse.getStatusCode().is2xxSuccessful()) {
                logger.info("Token validation successful");
                return true;
            } else {
                logger.warn("Token validation failed with status: {}", authResponse.getStatusCode());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization Token");
                return false;
            }
        } catch (Exception e) {
            logger.error("Auth service is unavailable: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Auth service is unavailable");
            return false;
        }
    }
}