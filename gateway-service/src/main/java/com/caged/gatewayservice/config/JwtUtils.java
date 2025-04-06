package com.caged.gatewayservice.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${application.security.jwt.secret-key}")
    private String jwtSecret;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    Boolean validateTokenExpiry(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        DecodedJWT decodedJWT = JWT.decode(token);
        System.out.println("validateTokenExpiry---datetime--"+decodedJWT.getExpiresAt());
        return decodedJWT.getExpiresAt().before(new Date());
        // return extractExpiration(token).before(new Date());
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /*  public boolean isAdmin(String token){
          Claims claims = extractAllClaims(token);
          List<String> allRoles = claims.get("Roles", List.class);
          return allRoles.contains(RoleName.ROLE_USER.toString());
      }*/
    public Long getUserId(String token){
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Claims claims = extractAllClaims(token);
        //  return new BigDecimal(String.valueOf(claims.get("userId"))).longValue();
        return Long.valueOf(claims.get("userId").toString());
    }
    public String getUserEmail(String token){
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Claims claims = extractAllClaims(token);
        return (String) claims.get("email");
    }
    public String getUserPhoneNumber(String token){
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Claims claims = extractAllClaims(token);
        return (String) claims.get("countryCode")+claims.get("phoneNumber");
    }

    public boolean isTokenExpired(String jwtToken){
        return validateTokenExpiry(jwtToken);
    }
}
