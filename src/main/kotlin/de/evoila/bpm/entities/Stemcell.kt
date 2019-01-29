package de.evoila.bpm.entities

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stemcell(
    @SerialName(value = "family")
    val family: String,
    @SerialName(value = "major_version")
    val majorVersion: Int,
    @SerialName(value = "minor_version")
    val minorVersion: Int
)