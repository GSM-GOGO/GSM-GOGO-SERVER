package team.gsmgogo.global.security.jwt;

import team.gsmgogo.global.exception.error.ExpectedException;
import team.gsmgogo.global.manager.CookieManager;
import team.gsmgogo.global.security.jwt.dto.TokenResponse;
import team.gsmgogo.global.security.principle.AuthDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AuthDetailsService authDetailsService;
    private final CookieManager cookieManager;

    @Value("${spring.jwt.secretKey}")
    private String secretKey;
    @Value("${spring.jwt.refreshKey}")
    private String refreshKey;
    @Value("${spring.jwt.accessExp}")
    public Long accessExp;
    @Value("${spring.jwt.refreshExp}")
    public Long refreshExp;

    public static final String ACCESS_KEY = "accessToken";
    public static final String REFRESH_KEY = "refreshToken";

    public TokenResponse getToken(String userSeq) {
        String accessToken = generateAccessToken(userSeq, accessExp);
        String refreshToken = generateRefrshToken(userSeq, refreshExp);

        return new TokenResponse(accessToken, refreshToken);
    }

    private String generateAccessToken(String userSeq, long expiration) {
        return Jwts.builder().signWith(SignatureAlgorithm.HS256, secretKey)
                .setSubject(userSeq)
                .setHeaderParam("typ", ACCESS_KEY)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .compact();
    }

    private String generateRefrshToken(String userSeq, long expiration) {
        return Jwts.builder().signWith(SignatureAlgorithm.HS256, refreshKey)
                .setSubject(userSeq)
                .setHeaderParam("typ", REFRESH_KEY)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .compact();
    }

    public String resolveAccessToken(HttpServletRequest request) {
        return cookieManager.getCookieValue(request, ACCESS_KEY);
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return cookieManager.getCookieValue(request, REFRESH_KEY);
    }

    public UsernamePasswordAuthenticationToken authorization(String token) {
        UserDetails userDetails = authDetailsService.loadUserByUsername(getTokenSubject(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private String getTokenSubject(String subject) {
        return getTokenBody(subject).getSubject();
    }

    private Claims getTokenBody(String token) {
        try {
            return Jwts.parser().setSigningKey(secretKey)
                    .parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new ExpectedException("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
        }
    }
}
