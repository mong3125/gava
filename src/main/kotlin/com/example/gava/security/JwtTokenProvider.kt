package com.example.gava.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

@Service
class JwtTokenProvider(
    @Value("\${jwt.secret-key}") private val secretKey: String,
    @Value("\${jwt.expiration}") private val validityInMs: Long,
    @Value("\${jwt.refresh-expiration}") private val refreshValidityInMs: Long
) {

    private val key: Key = Keys.hmacShaKeyFor(secretKey.toByteArray())

    fun createToken(username: String, roles: List<String>): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims["roles"] = roles

        val now = Date()
        val validity = Date(now.time + validityInMs)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun createRefreshToken(username: String?): String {
        val now = Date()
        val validity = Date(now.time + refreshValidityInMs)

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUsername(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            !claims.body.expiration.before(Date()) // 만료시간 지났는지 체크
        } catch (e: Exception) {
            false
        }
    }

    fun getExpirationTime(): Long {
        return validityInMs
    }
}
