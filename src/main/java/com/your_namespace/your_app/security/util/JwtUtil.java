package com.your_namespace.your_app.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil
{
    private final JwtParser jwtParser;

    private final SecretKey jwtSecretKey;


    /**
     * @param jwtSecretKeyValue generate with {@code openssl rand -hex 32}
     */
    public JwtUtil(@Value("${your_app.security.jwt.signing-key}") String jwtSecretKeyValue)
    {
        jwtSecretKey = new SecretKeySpec(jwtSecretKeyValue.trim().getBytes(), "HmacSHA256");
        this.jwtParser = Jwts.parser().verifyWith(jwtSecretKey).build();
    }


    /**
     * @return a signed, url-encoded JWS token as a string (inspect at <a href="https://jwt.io">https://jwt.io</a>)
     */
    public String buildToken(String subject, Map<String, Object> claims, Date issuedAt, Date expiration)
    {
        return Jwts.builder()
            .claims(Jwts.claims()
                .subject(subject)
                .add(claims)
                .build()
            )
            .issuedAt(issuedAt)
            .expiration(expiration)
            .signWith(jwtSecretKey)
            .compact();
    }

    /**
     * @return a Java-friendly claims object from a signed, url-encoded JWS token string
     */
    public Jws<Claims> resolveClaims(String claims)
    {
        return jwtParser.parseSignedClaims(claims);
    }
}