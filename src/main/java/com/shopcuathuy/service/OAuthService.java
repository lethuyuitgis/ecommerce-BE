package com.shopcuathuy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcuathuy.dto.AuthResponse;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.util.JwtUtil;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${oauth.google.client-id:}")
    private String googleClientId;

    @Value("${oauth.facebook.app-id:}")
    private String facebookAppId;

    @Value("${oauth.facebook.app-secret:}")
    private String facebookAppSecret;

    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        try {
            // Verify Google ID token
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Invalid Google token");
            }

            JsonNode userInfo = objectMapper.readTree(response.getBody());
            String email = userInfo.get("email").asText();
            String name = userInfo.get("name").asText();
            String picture = userInfo.has("picture") ? userInfo.get("picture").asText() : null;
            String googleId = userInfo.get("sub").asText();

            // Find or create user
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                // Update avatar if available
                if (picture != null && user.getAvatarUrl() == null) {
                    user.setAvatarUrl(picture);
                    userRepository.save(user);
                }
            } else {
                // Create new user
                user = new User();
                user.setEmail(email);
                user.setFullName(name);
                user.setAvatarUrl(picture);
                user.setUserType(User.UserType.CUSTOMER);
                user.setStatus(User.UserStatus.ACTIVE);
                // Set a random password (won't be used for OAuth users)
                user.setPasswordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
                user = userRepository.save(user);
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getUserType().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setRefreshToken(refreshToken);
            authResponse.setUserId(user.getId());
            authResponse.setEmail(user.getEmail());
            authResponse.setFullName(user.getFullName());
            authResponse.setUserType(user.getUserType().name());

            return authResponse;
        } catch (Exception e) {
            log.error("Error logging in with Google", e);
            throw new RuntimeException("Failed to login with Google: " + e.getMessage());
        }
    }

    @Transactional
    public AuthResponse loginWithFacebook(String accessToken) {
        try {
            // Verify Facebook access token
            String url = String.format(
                "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=%s",
                accessToken
            );
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Invalid Facebook token");
            }

            JsonNode userInfo = objectMapper.readTree(response.getBody());
            
            if (userInfo.has("error")) {
                throw new RuntimeException("Facebook API error: " + userInfo.get("error").get("message").asText());
            }

            String email = userInfo.has("email") ? userInfo.get("email").asText() : null;
            String name = userInfo.has("name") ? userInfo.get("name").asText() : null;
            String picture = userInfo.has("picture") && userInfo.get("picture").has("data") 
                ? userInfo.get("picture").get("data").get("url").asText() 
                : null;
            String facebookId = userInfo.get("id").asText();

            if (email == null) {
                // Try to get email with app access token
                String appAccessTokenUrl = String.format(
                    "https://graph.facebook.com/oauth/access_token?client_id=%s&client_secret=%s&grant_type=client_credentials",
                    facebookAppId, facebookAppSecret
                );
                ResponseEntity<String> appTokenResponse = restTemplate.getForEntity(appAccessTokenUrl, String.class);
                
                if (appTokenResponse.getStatusCode() == HttpStatus.OK) {
                    JsonNode appToken = objectMapper.readTree(appTokenResponse.getBody());
                    String appTokenValue = appToken.get("access_token").asText();
                    
                    // Use app token to get user email
                    String emailUrl = String.format(
                        "https://graph.facebook.com/%s?fields=email&access_token=%s",
                        facebookId, appTokenValue
                    );
                    ResponseEntity<String> emailResponse = restTemplate.getForEntity(emailUrl, String.class);
                    if (emailResponse.getStatusCode() == HttpStatus.OK) {
                        JsonNode emailInfo = objectMapper.readTree(emailResponse.getBody());
                        if (emailInfo.has("email")) {
                            email = emailInfo.get("email").asText();
                        }
                    }
                }
            }

            if (email == null) {
                // Generate email from Facebook ID if email is not available
                email = "facebook_" + facebookId + "@facebook.com";
            }

            // Find or create user
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                // Update avatar if available
                if (picture != null && user.getAvatarUrl() == null) {
                    user.setAvatarUrl(picture);
                    userRepository.save(user);
                }
            } else {
                // Create new user
                user = new User();
                user.setEmail(email);
                user.setFullName(name != null ? name : "Facebook User");
                user.setAvatarUrl(picture);
                user.setUserType(User.UserType.CUSTOMER);
                user.setStatus(User.UserStatus.ACTIVE);
                // Set a random password (won't be used for OAuth users)
                user.setPasswordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
                user = userRepository.save(user);
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getUserType().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setRefreshToken(refreshToken);
            authResponse.setUserId(user.getId());
            authResponse.setEmail(user.getEmail());
            authResponse.setFullName(user.getFullName());
            authResponse.setUserType(user.getUserType().name());

            return authResponse;
        } catch (Exception e) {
            log.error("Error logging in with Facebook", e);
            throw new RuntimeException("Failed to login with Facebook: " + e.getMessage());
        }
    }
}

