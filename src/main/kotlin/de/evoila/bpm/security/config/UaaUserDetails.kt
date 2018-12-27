package de.evoila.bpm.security.config

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.ArrayList

class UaaUserDetails : UserDetails {

  private var username: String? = null

  private val password: String? = null

  private var grantedAuthorities: List<GrantedAuthority> = ArrayList()

  override fun getAuthorities(): Collection<GrantedAuthority> {
    return this.grantedAuthorities
  }

  fun setGrantedAuthorities(grantedAuthorities: List<GrantedAuthority>) {
    this.grantedAuthorities = grantedAuthorities
  }

  fun setUsername(username: String) {
    this.username = username
  }

  override fun getPassword(): String? {
    return this.password
  }

  override fun getUsername(): String? {
    return this.username
  }

  override fun isAccountNonExpired(): Boolean {
    return true
  }

  override fun isAccountNonLocked(): Boolean {
    return true
  }

  override fun isCredentialsNonExpired(): Boolean {
    return true
  }

  override fun isEnabled(): Boolean {
    return true
  }
}
