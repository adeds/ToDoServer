package id.ade.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import id.ade.models.User
import id.ade.util.Constant
import id.ade.util.Constant.Environment.Auth.AUTHENTICATION
import id.ade.util.Constant.Environment.Auth.JWT_SECRET
import id.ade.util.Constant.General.PROJECT_NAME
import java.util.*

class JwtService {

    private val issuer = PROJECT_NAME
    private val jwtSecret = System.getenv(JWT_SECRET) // 1
    private val algorithm = Algorithm.HMAC512(jwtSecret)

    // 2
    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    // 3
    fun generateToken(user: User): String = JWT.create()
        .withSubject(AUTHENTICATION)
        .withIssuer(issuer)
        .withClaim(Constant.DatabaseKey.User.KEY_ID, user.userId)
        .withExpiresAt(expiresAt())
        .sign(algorithm)

    private fun expiresAt() =
        Date(System.currentTimeMillis() + 3_600_000 * 24) // 24 hours
}
