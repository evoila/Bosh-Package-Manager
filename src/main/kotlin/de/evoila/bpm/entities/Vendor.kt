package de.evoila.bpm.entities

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Vendor(

    val name: String,
    val members: Set<String>,
    override var id: String = UUID.randomUUID().toString()
) : BaseEntity() {

  fun isMember(name: String): Boolean = name in members
}