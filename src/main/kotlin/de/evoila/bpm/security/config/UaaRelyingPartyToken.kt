package de.evoila.bpm.security.config

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UaaRelyingPartyToken(
    val token: String?,
    private val principal: Any?,
    authorities: Collection<GrantedAuthority>?
) : AbstractAuthenticationToken(authorities) {

  constructor(userDetails: UserDetails, authorities: Collection<GrantedAuthority>?) : this(
      token = null,
      principal = userDetails,
      authorities = authorities) {

    super.setAuthenticated(true)
  }

  override fun getPrincipal(): Any? {
    return principal
  }

  @Throws(IllegalArgumentException::class)
  override fun setAuthenticated(isAuthenticated: Boolean) {
    if (isAuthenticated) {
      throw IllegalArgumentException(
          "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead")
    }

    super.setAuthenticated(false)
  }

  override fun getCredentials(): Any? {
    return null
  }
}