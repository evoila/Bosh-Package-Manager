package de.evoila.bpm.security.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.Table

@Table(name = "vendors")
@Entity
data class Vendor(
    val name: String
) : BaseEntity() {

  @OneToMany(mappedBy = "vendor")
  @JsonManagedReference
  lateinit var users: List<User>
}
