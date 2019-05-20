package de.evoila.bpm.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dependency(
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "version")
    val version: String,
    @SerialName(value = "publisher")
    val publisher: String
)