package de.evoila.bpm.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class Stemcell(
    @JsonProperty(value = "family")
    val family: String,
    @JsonProperty(value = "major_version")
    val majorVersion: Int,
    @JsonProperty(value = "minor_version")
    val minorVersion: Int
)