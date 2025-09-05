package com.momo.momo_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessValidityMs;   // 기본 1시간
    private final long refreshValidityMs;  // 기본 14일
    private static final String USER_NO_CLAIM_KEY = "userNo";

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-validity-ms:3600000}") long accessValidityMs,
            @Value("${jwt.refresh-validity-ms:1209600000}") long refreshValidityMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessValidityMs = accessValidityMs;
        this.refreshValidityMs = refreshValidityMs;
    }

    /** Access 토큰: sub=loginId, claim=userNo */
    public String createAccessToken(Long userNo, String loginId) {
        long now = System.currentTimeMillis();
        Date iat = new Date(now);
        Date exp = new Date(now + accessValidityMs);

        // 주의: setClaims(...)는 기존 클레임을 모두 덮어씌웁니다.
        return Jwts.builder()
                .setSubject(loginId)          // sub 유지
                .claim(USER_NO_CLAIM_KEY, userNo)      // 필요한 클레임만 개별 추가
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Refresh 토큰: sub=loginId, claim=userNo (동일 포맷) */
    public String createRefreshToken(Long userNo, String loginId) {
        long now = System.currentTimeMillis();
        Date iat = new Date(now);
        Date exp = new Date(now + refreshValidityMs);

        return Jwts.builder()
                .setSubject(loginId)
                .claim(USER_NO_CLAIM_KEY, userNo)
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 컨트롤러/필터에서 헤더든 원시토큰이든 둘 다 넣어도 동작하도록 */
    public boolean validateToken(String tokenOrBearer) {
        String token = resolveBearer(tokenOrBearer);
        if (token == null) return false;
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT 만료: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 무효: {}", e.getMessage());
        }
        return false;
    }

    public Long getUserNo(String tokenOrBearer) {
        String token = resolveBearer(tokenOrBearer);
        if (token == null) return null;
        Claims claims = parseClaims(token);
        if (claims == null) return null;
        Object v = claims.get(USER_NO_CLAIM_KEY);
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); }
        catch (NumberFormatException e) { return null; }
    }

    /** = loginId (subject) */
    public String getUserIdFromToken(String tokenOrBearer) {
        String token = resolveBearer(tokenOrBearer);
        if (token == null) return null;
        Claims claims = parseClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public Date getExpirationDateFromToken(String tokenOrBearer) {
        String token = resolveBearer(tokenOrBearer);
        Claims claims = parseClaims(token);
        return claims != null ? claims.getExpiration() : new Date(0);
    }

    public long getRefreshTokenValidity() {
        return refreshValidityMs;
    }

    /** HttpServletRequest에서 Authorization: Bearer ... 추출 */
    @Nullable
    public String resolveToken(HttpServletRequest request) {
        return resolveBearer(request.getHeader("Authorization"));
    }

    /** "Bearer xxx" 또는 순수 토큰 모두 허용해서 순수 토큰으로 변환 */
    @Nullable
    public String resolveBearer(@Nullable String headerOrToken) {
        if (headerOrToken == null || headerOrToken.isBlank()) return null;
        String s = headerOrToken.trim();
        if (s.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return s.substring(7).trim();
        }
        return s;
    }

    @Nullable
    private Claims parseClaims(@Nullable String rawToken) {
        if (rawToken == null) return null;
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(rawToken).getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 파싱 실패: {}", e.getMessage());
            return null;
        }
    }
}
