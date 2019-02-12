package de.evoila.bpm.rest.bodies

import com.fasterxml.jackson.annotation.JsonProperty
import de.evoila.bpm.entities.Dependency
import de.evoila.bpm.entities.Stemcell
import java.net.URL

data class PackageBody(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("version")
    val version: String,
    @JsonProperty("vendor")
    val vendor: String,
    @JsonProperty("files")
    val files: List<String>,
    @JsonProperty("dependencies")
    val dependencies: List<Dependency>?,
    @JsonProperty(value = "stemcell")
    val stemcell: Stemcell?,
    @JsonProperty("description")
    val description: String?,
    @JsonProperty("url")
    val url: String?
)