package de.evoila.bpm.rest.bodies

import com.fasterxml.jackson.annotation.JsonProperty

data class PackageBody(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("version")
    val version: String,
    @JsonProperty("vendor")
    val vendor: String,
    @JsonProperty("files")
    val files: List<String>
)