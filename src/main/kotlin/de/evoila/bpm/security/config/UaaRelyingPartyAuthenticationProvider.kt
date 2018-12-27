package de.evoila.bpm.security.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

open class UaaRelyingPartyAuthenticationProvider : AuthenticationProvider, InitializingBean {

  lateinit var publicKey: String

  private var authoritiesMapper: GrantedAuthoritiesMapper = NullAuthoritiesMapper()

  override fun authenticate(authentication: Authentication): Authentication? {
    if (!supports(authentication::class.java)) {
      return null
    }

    val auth = authentication as UaaRelyingPartyToken
    val tokenObj = UaaFilterUtils.verifiedToken(auth.token!!, publicKey)

    val userDetails = UaaUserDetails()
    userDetails.setUsername(tokenObj[USER_NAME].toString())
    userDetails.setGrantedAuthorities(scopeToGrantedAuthority(tokenObj[SCOPE] as List<String>))

    if (!userDetails.isEnabled) {
      throw AuthenticationServiceException("User is disabled")
    }

    return createSuccessfulAuthentication(userDetails)
  }

  private fun scopeToGrantedAuthority(scopes: List<String>): List<GrantedAuthority> {
    val grantedAuthorities = ArrayList<GrantedAuthority>()
    for (scope in scopes) {
      grantedAuthorities.add(ScopeAuthority(scope))
    }

    return grantedAuthorities
  }

  private fun createSuccessfulAuthentication(userDetails: UserDetails): Authentication {
    return UaaRelyingPartyToken(userDetails, authoritiesMapper.mapAuthorities(userDetails.authorities))
  }

  override fun supports(authentication: Class<*>?): Boolean {
    return UaaRelyingPartyToken::class.java.isAssignableFrom(authentication)
  }

  override fun afterPropertiesSet() {
    log.info("is set")
  }

  companion object {
    private val log = LoggerFactory.getLogger(UaaRelyingPartyAuthenticationProvider::class.java)
    const val EXP = "exp"
    const val CLIENT = "client"
    const val ORIGIN = "origin"
    const val SCOPE = "scope"
    const val SUB = "sub"
    const val USER_NAME = "user_name"
  }
}
