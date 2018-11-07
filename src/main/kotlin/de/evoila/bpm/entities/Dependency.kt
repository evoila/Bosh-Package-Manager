package de.evoila.bpm.entities

import com.fasterxml.jackson.annotation.JsonProperty


data class Dependency(
    @JsonProperty(value = "name")
    val name: String,
    @JsonProperty(value = "version")
    val version: String,
    @JsonProperty(value = "vendor")
    val vendor: String
)