package lingvo.app.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lingvo.app.auth.JwtTokenProvider;
import lingvo.app.auth.dto.LoginRequestDto;
import lingvo.app.auth.dto.RefreshTokenRequestDto;
import lingvo.app.auth.dto.SignUpRequestDto;
import lingvo.app.auth.entity.Role;
import lingvo.app.auth.entity.RoleType;
import lingvo.app.auth.entity.User;
import lingvo.app.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.Collections;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setHandlerExceptionResolvers(new ExceptionHandlerExceptionResolver())
                .build();
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnTokens() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(tokenProvider.createAccessToken(loginRequest.getUsername())).thenReturn("access_token");
        when(tokenProvider.createRefreshToken(loginRequest.getUsername())).thenReturn("refresh_token");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"));
    }

    @Test
    void authenticateUser_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Authentication failed: Invalid credentials"));
    }

    @Test
    void registerUser_WithValidData_ShouldReturnSuccess() throws Exception {
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUsername("newuser");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("password123");

        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void registerUser_WithExistingUsername_ShouldReturnBadRequest() throws Exception {
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUsername("existinguser");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("password123");

        when(userService.existsByUsername("existinguser")).thenReturn(true);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken!"));
    }

    @Test
    void getCurrentUser_WithAuthenticatedUser_ShouldReturnUserInfo() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        Role role = new Role();
        role.setName(RoleType.USER);
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        when(userService.findByUsername("testuser")).thenReturn(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(get("/api/auth/user")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.role[0].name").value("USER"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WithUnauthenticatedUser_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() throws Exception {
        RefreshTokenRequestDto refreshTokenRequest = new RefreshTokenRequestDto();
        refreshTokenRequest.setRefreshToken("valid_refresh_token");

        when(tokenProvider.validateToken("valid_refresh_token")).thenReturn(true);
        when(tokenProvider.getUsername("valid_refresh_token")).thenReturn("testuser");
        when(tokenProvider.createAccessToken("testuser")).thenReturn("new_access_token");
        when(tokenProvider.createRefreshToken("testuser")).thenReturn("new_refresh_token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new_access_token"))
                .andExpect(jsonPath("$.refreshToken").value("new_refresh_token"));
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        RefreshTokenRequestDto refreshTokenRequest = new RefreshTokenRequestDto();
        refreshTokenRequest.setRefreshToken("invalid_refresh_token");

        when(tokenProvider.validateToken("invalid_refresh_token")).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid refresh token"));
    }

    @Test
    void revokeToken_WithValidToken_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/revoke")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token revoked successfully"));

        verify(tokenProvider).revokeToken("valid_token");
    }

    @Test
    void revokeToken_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/revoke")
                        .header("Authorization", "InvalidToken"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void logout_WithValidToken_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));

        verify(tokenProvider).revokeToken("valid_token");
    }

    @Test
    void logout_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "InvalidToken"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));
    }
}