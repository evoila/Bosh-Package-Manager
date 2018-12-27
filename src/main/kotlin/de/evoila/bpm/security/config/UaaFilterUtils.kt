package de.evoila.bpm.security.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.jwt.Jwt
import org.springframework.security.jwt.JwtHelper
import org.springframework.security.jwt.crypto.sign.RsaVerifier
import org.springframework.util.Assert
import java.io.IOException
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.constraints.NotNull

object UaaFilterUtils {

  private const val BEARER = "Bearer "
  private val log = LoggerFactory.getLogger(UaaFilterUtils::class.java)

  var objectMapper = ObjectMapper()

  fun tryResolveToken(request: HttpServletRequest, headerName: String): String? {
    Assert.notNull(headerName, "headerName must not be null/or empty")
    val headerValue = request.getHeader(headerName)
    return if (headerValue == null || !headerValue.startsWith(BEARER)) {
      null
    } else headerValue.substring(BEARER.length)

  }

  @NotNull
  fun verifiedToken(token: String, publicKey: String): Map<String, Any> {
    val jwt = JwtHelper.decode(token)

    // Currently not sure how we should handle this because we have multiple
    // CF instances. We would need to have a central file for all UAA
    // instances
    // verifySignature(jwt, publicKey);

    val tokenObj = tryExtractToken(jwt)
        ?: throw AuthenticationServiceException("Error parsing JWT token/extracting claims")

    verifyExpiration(tokenObj)
    return tokenObj
  }

  private fun verifyExpiration(tokenObj: Map<String, Any>) {
    val timestamp = tokenObj[UaaRelyingPartyAuthenticationProvider.EXP] as Int as Long * 1000
    val now = Date()
    val expirationTime = Date(timestamp)
    if (!now.before(expirationTime)) {
      throw AuthenticationServiceException("Token expiration timed out")
    }
  }

  private fun verifySignature(jwt: Jwt, publicKey: String) {
    try {
      val rsaVerifier = RsaVerifier(publicKey)
      jwt.verifySignature(rsaVerifier)
    } catch (ex: Exception) {
      throw AuthenticationServiceException("Error verifying signature of token")
    }

  }

  private fun tryExtractToken(jwt: Jwt): Map<String, Any>? {
    if (jwt.claims == null)
      return null

    try {
      return objectMapper.readValue(jwt.claims, object : TypeReference<HashMap<String, Any>>() {

      })
    } catch (e: IOException) {
      log.error("Error parsing claims from JWT", e)
    }

    return null
  }
}
