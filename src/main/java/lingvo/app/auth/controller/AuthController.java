package lingvo.app.auth.controller;

import jakarta.validation.Valid;
import lingvo.app.auth.JwtTokenProvider;
import lingvo.app.auth.dto.AuthResponseDto;
import lingvo.app.auth.dto.LoginRequestDto;
import lingvo.app.auth.dto.RefreshTokenRequestDto;
import lingvo.app.auth.dto.SignUpRequestDto;
import lingvo.app.auth.entity.User;
import lingvo.app.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        logger.info("Attempting to authenticate user: {}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = tokenProvider.createAccessToken(authentication.getName());
            String refreshToken = tokenProvider.createRefreshToken(authentication.getName());
            logger.info("User {} successfully authenticated", loginRequest.getUsername());
            return ResponseEntity.ok(new AuthResponseDto(accessToken, refreshToken));
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequestDto signUpRequest) {
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        if (userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already in use!");
        }

        userService.createUser(signUpRequest);

        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        logger.info("Попытка получить информацию о текущем пользователе");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                response.put("role", user.getRoles());

                logger.info("Информация о текущем пользователе успешно получена для пользователя: {}", username);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь не найден в базе данных: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Пользователь не найден"));
            }
        } else {
            logger.warn("Аутентифицированный пользователь не найден");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Аутентифицированный пользователь не найден"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        if (tokenProvider.validateToken(refreshToken)) {
            String username = tokenProvider.getUsername(refreshToken);
            String newAccessToken = tokenProvider.createAccessToken(username);
            String newRefreshToken = tokenProvider.createRefreshToken(username);
            return ResponseEntity.ok(new AuthResponseDto(newAccessToken, newRefreshToken)); // Добавлены аргументы в конструктор
        }
        return ResponseEntity.badRequest().body("Invalid refresh token");
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revokeToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            tokenProvider.revokeToken(token);
            return ResponseEntity.ok("Token revoked successfully");
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            tokenProvider.revokeToken(token);
            return ResponseEntity.ok("Logged out successfully");
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }


}
