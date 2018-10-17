package de.evoila.bpm.entities

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "releases")
data class Package(
    @JsonProperty(value = "name")
    val name: String,
    @JsonProperty(value = "blobs")
    val blobs: List<String>,
    @JsonProperty(value = "dependencies")
    val dependencies: List<String>,
    @JsonProperty(value = "spec")
    val spec: String,
    @JsonProperty(value = "packaging")
    val packaging: String,
    @JsonProperty(value = "version")
    val version: String
) : BaseEntity()