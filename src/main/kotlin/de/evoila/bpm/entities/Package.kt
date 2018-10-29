package de.evoila.bpm.entities

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "packages")
data class Package(
    @JsonProperty(value = "name")
    val name: String,
    @JsonProperty(value = "version")
    val version: String,
    @JsonProperty(value = "vendor")
    val vendor: String,
    @JsonProperty(value = "s3location")
    val s3location: String,
    @JsonProperty(value = "files")
    val files: List<String>
) : BaseEntity()