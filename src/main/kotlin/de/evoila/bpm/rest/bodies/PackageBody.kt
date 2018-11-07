package de.evoila.bpm.rest.bodies

import com.fasterxml.jackson.annotation.JsonProperty
import de.evoila.bpm.entities.Dependency
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

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
    @Field(type = FieldType.Nested)
    val dependencies: List<Dependency>?
)