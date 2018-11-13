package de.evoila.bpm.security.model

import com.fasterxml.jackson.annotation.JsonBackReference
import org.springframework.security.core.GrantedAuthority
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Table(name = "roles")
@Entity
data class UserRole(
    var role: Role,
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    val user: User
) : BaseEntity(), GrantedAuthority {

  override fun getAuthority(): String {
    return role.name
  }

  enum class Role {
    ADMIN, GUEST, VENDOR
  }
}