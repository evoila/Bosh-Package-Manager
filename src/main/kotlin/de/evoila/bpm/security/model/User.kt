package de.evoila.bpm.security.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.annotation.Transactional
import javax.persistence.*

@Table(name = "users")
@Entity
class User(
    private val username: String,
    @JsonIgnore
    private val password: String,
    @JsonIgnore
    val email: String,
    @Column(name = "signing_key")
    @JsonIgnore
    val signingKey: String
) : BaseEntity(), UserDetails {

  @JsonIgnore
  @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
  lateinit var roles: MutableList<UserRole>

  @ManyToMany(mappedBy = "members", cascade = [CascadeType.MERGE])
  @LazyCollection(value = LazyCollectionOption.FALSE)
  lateinit var memberOf: Set<Vendor>

  @ManyToMany(mappedBy = "admins", cascade = [CascadeType.MERGE])
  @LazyCollection(value = LazyCollectionOption.FALSE)
  lateinit var adminOf: Set<Vendor>

  @JsonIgnore
  override fun getPassword(): String = password

  override fun getUsername(): String = username

  @JsonIgnore
  override fun getAuthorities(): MutableCollection<out GrantedAuthority> = roles

  @JsonIgnore
  override fun isEnabled(): Boolean = true

  @JsonIgnore
  override fun isCredentialsNonExpired(): Boolean = true

  @JsonIgnore
  override fun isAccountNonExpired(): Boolean = true

  @JsonIgnore
  override fun isAccountNonLocked(): Boolean = true
}