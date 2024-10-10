package ru.skillbox.mc_account.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.skillbox.mc_account.security.CustomUserDetailsService;
import ru.skillbox.mc_account.exception.InvalidInputException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private RestTemplate restTemplate;
    private final String AUTH_SERVICE_URL = "http://auth-service/api/v1/auth/validate";

    private static final ThreadLocal<String> currentToken = new ThreadLocal<>();

    // Флаг для временной заглушки
    private boolean bypassAuthCheck = true;

    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public AuthFilter(RestTemplate restTemplate, CustomUserDetailsService userDetailsService) {
        this.restTemplate = restTemplate;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authToken = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        String requestURI = httpRequest.getRequestURI();

        currentToken.set(authToken);

        if (isSwaggerRequest(requestURI)) {
            logger.debug("Swagger request: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        if (bypassAuthCheck) {
            logger.info("Bypassing auth check for request: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        logger.info("Processing request: {} with token: {}", requestURI, authToken);

        String userEmail;
        try {
            userEmail = extractEmailFromCurrentToken();
        } catch (InvalidInputException e) {
            logger.warn("Invalid token: {}", e.getMessage());
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        if (!isValidToken(authToken, (HttpServletResponse) response)) {
            logger.warn("Invalid or missing Authorization token for request: {}", requestURI);
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        chain.doFilter(request, response);
    }

    public static String getCurrentToken() {
        return currentToken.get();
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
            ResponseEntity<Void> authResponse = restTemplate.getForEntity(AUTH_SERVICE_URL + "?token=" + token, Void.class);
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

    private String extractEmailFromCurrentToken() {
        String token = AuthFilter.getCurrentToken();
        if (token == null || token.trim().isEmpty()) {
            throw new InvalidInputException("Authorization token must not be empty");
        }

        String email;
        try {
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                throw new InvalidInputException("Invalid token format: Token must have 3 parts");
            }
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(payload);
            if (!jsonObject.has("email")) {
                throw new InvalidInputException("Email is missing from the token");
            }

            email = jsonObject.getString("email");
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid token format: Unable to decode payload");
        } catch (JSONException e) {
            throw new InvalidInputException("Invalid token format: Error parsing payload");
        } catch (Exception e) {
            throw new InvalidInputException("Invalid token format: " + e.getMessage());
        }

        return email;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void destroy() {
        currentToken.remove();
    }
}
