package lingvo.app.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration}")
    private long accessTokenValidityInMilliseconds;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenValidityInMilliseconds;

    private SecretKey secretKey;

    private UserDetailsService userDetailsService;

    private final ConcurrentHashMap<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    public JwtTokenProvider() {
        scheduler.scheduleAtFixedRate(this::cleanupBlacklistedTokens, 1, 1, TimeUnit.HOURS);
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        logger.info("JWT secret key initialized successfully");
    }

    public String createAccessToken(String username) {
        return createToken(username, accessTokenValidityInMilliseconds);
    }

    public String createRefreshToken(String username) {
        return createToken(username, refreshTokenValidityInMilliseconds);
    }

    private String createToken(String username, long validityInMilliseconds) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        logger.info("Created token for user {} with expiration {}", username, validity);
        return token;
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public void revokeToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
            Date expiration = claims.getExpiration();
            blacklistedTokens.put(token, expiration);
        } catch (ExpiredJwtException e) {
            // Токен уже истек, просто добавляем его в черный список
            blacklistedTokens.put(token, new Date());
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

//    public void revokeToken(String token) {
//        try {
//            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
//            Date expiration = claims.getExpiration();
//            blacklistedTokens.put(token, expiration);
//        } catch (JwtException e) {
//            throw new IllegalArgumentException("Invalid token");
//        }
//    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

            if (blacklistedTokens.containsKey(token)) {
                return false;
            }

            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            // Токен истек
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private void cleanupBlacklistedTokens() {
        Date now = new Date();
        int sizeBefore = blacklistedTokens.size();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
        int sizeAfter = blacklistedTokens.size();
        logger.info("Cleaned up {} expired tokens from blacklist", sizeBefore - sizeAfter);
    }


    public void shutdown() {
        scheduler.shutdown();
    }
}