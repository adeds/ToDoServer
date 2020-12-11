package id.ade.auth

import id.ade.util.Constant.Environment.Auth.HASH_ALGORITH
import id.ade.util.Constant.Environment.Auth.SECRET_KEY
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/*
    Makes use of the SECRET_KEY Environment Variable defined in step 2.
    Use this value as the argument of the hex function, which turns the HEX key into a ByteArray.
    Note the use of @KtorExperimentalAPI to avoid warnings associated with the experimental status of the hex function.
* */
@KtorExperimentalAPI
val hashKey = hex(System.getenv(SECRET_KEY)) // Defines Environment Variable.

@KtorExperimentalAPI
val hmacKey = SecretKeySpec(hashKey, HASH_ALGORITH) // Creates a secret key using the given algorithm, HmacSHA1.

//hash converts a password to a string hash.
@KtorExperimentalAPI
fun hash(password: String): String {
    val hmac = Mac.getInstance(HASH_ALGORITH)
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}
