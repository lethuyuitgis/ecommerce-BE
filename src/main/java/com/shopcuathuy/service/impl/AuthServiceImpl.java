package com.shopcuathuy.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shopcuathuy.dto.request.GoogleLoginRequestDTO;
import com.shopcuathuy.dto.request.LoginRequestDTO;
import com.shopcuathuy.dto.request.RefreshTokenRequestDTO;
import com.shopcuathuy.dto.request.RegisterRequestDTO;
import com.shopcuathuy.dto.response.AuthResponseDTO;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.UnauthorizedException;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.security.JwtTokenProvider;
import com.shopcuathuy.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Set<String> allowedGoogleAudiences;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           @Value("${google.oauth.client-ids:}") String googleAudiences) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        if (googleAudiences == null || googleAudiences.isBlank()) {
            this.allowedGoogleAudiences = Collections.emptySet();
        } else {
            this.allowedGoogleAudiences = Arrays.stream(googleAudiences.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        }
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request) {
        GoogleTokenInfo tokenInfo = verifyGoogleToken(request.idToken);

        String email = tokenInfo.getEmail();
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Google account is missing email");
        }

        User user = userRepository.findByEmail(email)
            .map(existing -> updateUserFromGoogle(existing, tokenInfo))
            .orElseGet(() -> createUserFromGoogle(tokenInfo));

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        com.shopcuathuy.util.ValidationUtil.validateEmail(request.email);
        com.shopcuathuy.util.ValidationUtil.validatePassword(request.password);

        if (userRepository.existsByEmail(request.email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (request.fullName == null || request.fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }

        User newUser = new User();
        newUser.setEmail(request.email);
        newUser.setPasswordHash(passwordEncoder.encode(request.password));
        newUser.setFullName(request.fullName);
        if (request.phone != null) {
            newUser.setPhone(request.phone);
        }
        newUser.setUserType(User.UserType.CUSTOMER);
        newUser.setStatus(User.UserStatus.ACTIVE);

        newUser = userRepository.save(newUser);
        return buildAuthResponse(newUser);
    }

    @Override
    public void logout(String token) {
        // Stateless JWT - no server-side logout required, reserved for future blacklist logic
    }

    @Override
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (jwtTokenProvider.getTokenType(request.refreshToken) != JwtTokenProvider.TokenType.REFRESH) {
            throw new UnauthorizedException("Invalid token type");
        }

        String userId = jwtTokenProvider.getUserId(request.refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO loginWithFacebook(String accessToken) {
        FacebookUserInfo userInfo = verifyFacebookToken(accessToken);

        String email = resolveFacebookEmail(userInfo);

        User user = userRepository.findByEmail(email)
            .map(existing -> updateUserFromFacebook(existing, userInfo))
            .orElseGet(() -> createUserFromFacebook(userInfo));

        return buildAuthResponse(user);
    }

    private User createUserFromFacebook(FacebookUserInfo userInfo) {
        User user = new User();
        user.setEmail(resolveFacebookEmail(userInfo));
        user.setFullName(userInfo.getName() != null && !userInfo.getName().isBlank()
            ? userInfo.getName()
            : userInfo.getEmail());
        user.setAvatarUrl(userInfo.getPicture() != null && userInfo.getPicture().getData() != null
            ? userInfo.getPicture().getData().getUrl()
            : null);
        user.setPasswordHash(passwordEncoder.encode("FACEBOOK-" + UUID.randomUUID()));
        user.setUserType(User.UserType.CUSTOMER);
        user.setStatus(User.UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private User updateUserFromFacebook(User user, FacebookUserInfo userInfo) {
        boolean changed = false;
        if (userInfo.getName() != null && !userInfo.getName().isBlank()) {
            if (user.getFullName() == null || user.getFullName().isBlank()
                || user.getFullName().equalsIgnoreCase(user.getEmail())) {
                user.setFullName(userInfo.getName());
                changed = true;
            }
        }
        if (userInfo.getPicture() != null && userInfo.getPicture().getData() != null) {
            String pictureUrl = userInfo.getPicture().getData().getUrl();
            if (pictureUrl != null && !pictureUrl.isBlank() && !pictureUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(pictureUrl);
                changed = true;
            }
        }
        if (changed) {
            return userRepository.save(user);
        }
        return user;
    }

    private FacebookUserInfo verifyFacebookToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new UnauthorizedException("Missing Facebook access token");
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/me")
                .queryParam("fields", "id,name,email,picture")
                .queryParam("access_token", accessToken)
                .toUriString();
            ResponseEntity<FacebookUserInfo> response = restTemplate.getForEntity(url, FacebookUserInfo.class);
            FacebookUserInfo info = response.getBody();
            if (info == null || info.getId() == null) {
                throw new UnauthorizedException("Invalid Facebook token");
            }
            return info;
        } catch (RestClientException ex) {
            throw new UnauthorizedException("Failed to verify Facebook token: " + ex.getMessage());
        }
    }

    private String resolveFacebookEmail(FacebookUserInfo userInfo) {
        if (userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) {
            return userInfo.getEmail();
        }
        if (userInfo.getId() == null || userInfo.getId().isBlank()) {
            throw new UnauthorizedException("Facebook account is missing id");
        }
        return "fb-" + userInfo.getId() + "@facebook.local";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FacebookUserInfo {
        private String id;
        private String name;
        private String email;
        private FacebookPicture picture;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public FacebookPicture getPicture() {
            return picture;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FacebookPicture {
        private FacebookPictureData data;

        public FacebookPictureData getData() {
            return data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FacebookPictureData {
        private String url;

        public String getUrl() {
            return url;
        }
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return new AuthResponseDTO(
            accessToken,
            refreshToken,
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getUserType().name(),
            user.getAvatarUrl()
        );
    }

    private User createUserFromGoogle(GoogleTokenInfo tokenInfo) {
        User user = new User();
        user.setEmail(tokenInfo.getEmail());
        user.setFullName(tokenInfo.getName() != null && !tokenInfo.getName().isBlank()
            ? tokenInfo.getName()
            : tokenInfo.getEmail());
        user.setAvatarUrl(tokenInfo.getPicture());
        user.setPasswordHash(passwordEncoder.encode("GOOGLE-" + UUID.randomUUID()));
        user.setUserType(User.UserType.CUSTOMER);
        user.setStatus(User.UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private User updateUserFromGoogle(User user, GoogleTokenInfo tokenInfo) {
        boolean changed = false;
        if (tokenInfo.getName() != null && !tokenInfo.getName().isBlank()) {
            if (user.getFullName() == null || user.getFullName().isBlank()
                || user.getFullName().equalsIgnoreCase(user.getEmail())) {
                user.setFullName(tokenInfo.getName());
                changed = true;
            }
        }
        if (tokenInfo.getPicture() != null && !tokenInfo.getPicture().isBlank()
            && !tokenInfo.getPicture().equals(user.getAvatarUrl())) {
            user.setAvatarUrl(tokenInfo.getPicture());
            changed = true;
        }
        if (changed) {
            return userRepository.save(user);
        }
        return user;
    }

    private GoogleTokenInfo verifyGoogleToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new UnauthorizedException("Missing Google token");
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://oauth2.googleapis.com/tokeninfo")
                .queryParam("id_token", idToken)
                .toUriString();
            ResponseEntity<GoogleTokenInfo> response = restTemplate.getForEntity(url, GoogleTokenInfo.class);
            GoogleTokenInfo info = response.getBody();
            if (info == null) {
                throw new UnauthorizedException("Invalid Google token");
            }

            if (!info.isEmailVerified()) {
                throw new UnauthorizedException("Google account email is not verified");
            }

            if (!allowedGoogleAudiences.isEmpty() && info.getAud() != null
                && !allowedGoogleAudiences.contains(info.getAud())) {
                throw new UnauthorizedException("Google token audience mismatch");
            }

            return info;
        } catch (RestClientException ex) {
            throw new UnauthorizedException("Failed to verify Google token");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleTokenInfo {
        private String aud;
        private String email;
        @JsonProperty("email_verified")
        private String emailVerified;
        private String name;
        private String picture;

        public String getAud() {
            return aud;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public String getPicture() {
            return picture;
        }

        public boolean isEmailVerified() {
            if (emailVerified == null) {
                return false;
            }
            return "true".equalsIgnoreCase(emailVerified) || "1".equals(emailVerified);
        }
    }
}


