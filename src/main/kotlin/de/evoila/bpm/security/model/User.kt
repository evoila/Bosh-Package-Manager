package de.evoila.bpm.security.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import javax.persistence.*

@Table(name = "users")
@Entity
class User(
    private val username: String,
    private val password: String,
    val email: String,
    @Column(name = "signing_key")
    @JsonIgnore
    val signingKey: String = UUID.randomUUID().toString()
) : BaseEntity(), UserDetails {

  @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
  lateinit var roles: MutableList<UserRole>

  @ManyToMany(mappedBy = "members")
  lateinit var memberOf: List<Vendor>

  @ManyToMany(mappedBy = "admins")
  lateinit var adminOf: List<Vendor>

  override fun getPassword(): String = password
  override fun getUsername(): String = username
  override fun getAuthorities(): MutableCollection<out GrantedAuthority> = roles
  override fun isEnabled(): Boolean = true
  override fun isCredentialsNonExpired(): Boolean = true
  override fun isAccountNonExpired(): Boolean = true
  override fun isAccountNonLocked(): Boolean = true
}