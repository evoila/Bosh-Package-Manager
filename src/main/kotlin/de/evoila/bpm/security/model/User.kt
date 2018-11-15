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
    private val password: String,
    val email: String,
    @Column(name = "signing_key")
    @JsonIgnore
    val signingKey: String
) : BaseEntity(), UserDetails {

  @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
  lateinit var roles: MutableList<UserRole>

  @ManyToMany(mappedBy = "members", cascade = [CascadeType.MERGE])
  lateinit var memberOf: Set<Vendor>

  @ManyToMany(mappedBy = "admins", cascade = [CascadeType.MERGE])
  @LazyCollection(LazyCollectionOption.EXTRA)
  lateinit var adminOf: Set<Vendor>

  override fun getPassword(): String = password
  override fun getUsername(): String = username
  override fun getAuthorities(): MutableCollection<out GrantedAuthority> = roles
  override fun isEnabled(): Boolean = true
  override fun isCredentialsNonExpired(): Boolean = true
  override fun isAccountNonExpired(): Boolean = true
  override fun isAccountNonLocked(): Boolean = true

  override fun equals(other: Any?): Boolean {

    return if (other == null || other !is User) {
      false
    } else {

      this.id == other.id
    }
  }
}