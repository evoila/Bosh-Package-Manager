package de.evoila.bpm.entities

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class User(
    override var id: String = UUID.randomUUID().toString(),
    val email: String
) : BaseEntity()