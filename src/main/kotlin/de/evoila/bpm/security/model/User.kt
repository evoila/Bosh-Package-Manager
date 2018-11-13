package de.evoila.bpm.security.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import javax.persistence.*

@Table(name = "users")
@Entity
class User(
    private val username: String,
    @JsonIgnore
    private val password: String,
    val email: String,
    @Column(name = "signing_key")
    @JsonIgnore
    val signingKey: String = UUID.randomUUID().toString(),
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    @JsonBackReference
    val vendor: Vendor?
) : BaseEntity(), UserDetails {

  @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
  @JsonManagedReference
  lateinit var roles: MutableList<UserRole>

  override fun getPassword(): String = password
  override fun getUsername(): String = username
  override fun getAuthorities(): MutableCollection<out GrantedAuthority> = roles
  override fun isEnabled(): Boolean = true
  override fun isCredentialsNonExpired(): Boolean = true
  override fun isAccountNonExpired(): Boolean = true
  override fun isAccountNonLocked(): Boolean = true
}