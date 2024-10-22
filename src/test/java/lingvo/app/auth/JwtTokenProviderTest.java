package lingvo.app.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    private static final String SECRET_KEY = "veryLongSecretKeyForTestingPurposesOnlyVeryLongSecretKeyForTestingPurposesOnly";
    private static final long ACCESS_TOKEN_VALIDITY = 3600000; // 1 hour
    private static final long REFRESH_TOKEN_VALIDITY = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtTokenProvider = new JwtTokenProvider();
        jwtTokenProvider.setUserDetailsService(userDetailsService);

        ReflectionTestUtils.setField(jwtTokenProvider, "secretKeyString", SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInMilliseconds", ACCESS_TOKEN_VALIDITY);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenValidityInMilliseconds", REFRESH_TOKEN_VALIDITY);

        jwtTokenProvider.init();
    }

    @Test
    void createAccessToken_ShouldCreateValidToken() {
        String username = "testuser";
        String token = jwtTokenProvider.createAccessToken(username);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.getUsername(token));
    }

    @Test
    void createRefreshToken_ShouldCreateValidToken() {
        String username = "testuser";
        String token = jwtTokenProvider.createRefreshToken(username);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.getUsername(token));
    }

    @Test
    void getAuthentication_ShouldReturnValidAuthentication() {
        String username = "testuser";
        String token = jwtTokenProvider.createAccessToken(username);
        UserDetails userDetails = new User(username, "", Collections.emptyList());

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void resolveToken_WithValidBearerToken_ShouldReturnToken() {
        String token = "validToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        String resolvedToken = jwtTokenProvider.resolveToken(request);

        assertEquals(token, resolvedToken);
    }

    @Test
    void resolveToken_WithInvalidHeader_ShouldReturnNull() {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        String resolvedToken = jwtTokenProvider.resolveToken(request);

        assertNull(resolvedToken);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtTokenProvider.createAccessToken("testuser");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WithBlacklistedToken_ShouldReturnFalse() {
        String token = jwtTokenProvider.createAccessToken("testuser");

        // Сначала токен должен быть действительным
        assertTrue(jwtTokenProvider.validateToken(token));

        // Отзываем токен
        jwtTokenProvider.revokeToken(token);

        // Теперь токен должен быть недействительным
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInMilliseconds", 1L); // 1 миллисекунда
        String token = jwtTokenProvider.createAccessToken("testuser");

        // Ждем, пока токен истечет
        Thread.sleep(10);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    void revokeToken_ShouldBlacklistToken() {
        String token = jwtTokenProvider.createAccessToken("testuser");

        jwtTokenProvider.revokeToken(token);

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void revokeToken_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalidToken";

        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.revokeToken(invalidToken));
    }

    @Test
    void revokeToken_WithExpiredToken_ShouldNotThrowException() {
        // Устанавливаем отрицательное время жизни токена, чтобы он сразу стал недействительным
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInMilliseconds", -1000L);
        String token = jwtTokenProvider.createAccessToken("testuser");

        // Этот вызов не должен выбросить исключение
        assertDoesNotThrow(() -> jwtTokenProvider.revokeToken(token));

        // Токен должен быть недействительным
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void cleanupBlacklistedTokens_ShouldRemoveExpiredTokens() throws Exception {
        // Установим короткое время жизни токена
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInMilliseconds", 500L);

        String token = jwtTokenProvider.createAccessToken("testuser");

        // Сразу же отзываем токен
        jwtTokenProvider.revokeToken(token);

        // Проверяем, что токен недействителен сразу после отзыва
        assertFalse(jwtTokenProvider.validateToken(token));

        // Ждем, пока токен "истечет"
        Thread.sleep(600);

        // Вызываем метод очистки
        ReflectionTestUtils.invokeMethod(jwtTokenProvider, "cleanupBlacklistedTokens");

        // Теперь токен должен быть действительным, так как он истек и был удален из черного списка
        assertTrue(jwtTokenProvider.validateToken(token));

        // Создаем новый токен
        String newToken = jwtTokenProvider.createAccessToken("testuser");

        // Новый токен должен быть действительным
        assertTrue(jwtTokenProvider.validateToken(newToken));
    }

    @Test
    void cleanupBlacklistedTokens_ShouldRemoveOnlyExpiredTokens() throws Exception {
        // Установим короткое время жизни токена для первого токена
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInMilliseconds", 500L);

        String expiredToken = jwtTokenProvider.createAccessToken("testuser1");

        // Установим более длительное время жизни для второго токена
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInMilliseconds", 5000L);

        String validToken = jwtTokenProvider.createAccessToken("testuser2");

        // Отзываем оба токена
        jwtTokenProvider.revokeToken(expiredToken);
        jwtTokenProvider.revokeToken(validToken);

        // Ждем, пока первый токен истечет
        Thread.sleep(600);

        // Вызываем метод очистки
        ReflectionTestUtils.invokeMethod(jwtTokenProvider, "cleanupBlacklistedTokens");

        // Проверяем, что истекший токен удален из черного списка, но все еще недействителен из-за истечения срока
        assertFalse(jwtTokenProvider.validateToken(expiredToken));

        // Проверяем, что действительный токен все еще в черном списке и недействителен
        assertFalse(jwtTokenProvider.validateToken(validToken));
    }
}