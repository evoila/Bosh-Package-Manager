package de.evoila.bpm.security.model

import javax.persistence.*

@Table(name = "vendors")
@Entity
data class Vendor(
    val name: String
) : BaseEntity() {

  @ManyToMany(cascade = [CascadeType.ALL])
  @JoinTable(
      name = "vendor_members",
      joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
      inverseJoinColumns = [JoinColumn(name = "vendor_id", referencedColumnName = "id")])
  lateinit var members: List<User>

  @ManyToMany(cascade = [CascadeType.ALL])
  @JoinTable(
      name = "vendor_admins",
      joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
      inverseJoinColumns = [JoinColumn(name = "vendor_id", referencedColumnName = "id")])
  lateinit var admins: List<User>
}
