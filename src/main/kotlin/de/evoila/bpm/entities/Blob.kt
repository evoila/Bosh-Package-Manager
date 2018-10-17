package de.evoila.bpm.entities

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "blobs")
data class Blob(
    @JsonProperty(value = "name")
    val name: String,
    @JsonProperty(value = "location")
    var location: String? = null,
    @JsonProperty(value = "version")
    val version: String
) : BaseEntity()