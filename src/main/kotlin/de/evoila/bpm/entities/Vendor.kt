package de.evoila.bpm.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class Vendor(
    @JsonProperty(value = "name")
    val name: String,
    @JsonProperty(value = "members")
    val members: List<String>
) : BaseEntity() {

  fun isMember(name: String): Boolean = name in members
}