package de.evoila.bpm.security.config

import org.springframework.security.core.GrantedAuthority

class ScopeAuthority(private val authority: String) : GrantedAuthority {

  override fun getAuthority(): String = authority
}
