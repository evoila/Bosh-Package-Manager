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
    @JsonProperty(value = "uploadDate")
    val uploadDate: String,
    @JsonProperty(value = "files")
    val files: List<String>,
    @JsonProperty("dependencies")
    val dependencies: List<Dependency>?,
    @JsonProperty("accessLevel")
    val accessLevel: AccessLevel,
    @JsonProperty(value = "stemcell")
    val stemcell: Stemcell?,
    @JsonProperty(value = "signed_with")
    val signedWith: String
) : BaseEntity() {

  enum class AccessLevel {
    PRIVATE, VENDOR, PUBLIC
  }
}

