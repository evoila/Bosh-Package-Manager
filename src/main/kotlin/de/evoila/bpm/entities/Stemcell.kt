package de.evoila.bpm.entities

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stemcell(
    @JsonProperty(value = "family")
    @SerialName(value = "family")
    val family: String,
    @JsonProperty(value = "major_version")
    @SerialName(value = "major_version")
    val majorVersion: Int,
    @JsonProperty(value = "minor_version")
    @SerialName(value = "minor_version")
    val minorVersion: Int
) : java.io.Serializable