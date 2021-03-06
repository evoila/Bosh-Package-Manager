package de.evoila.bpm.entities

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Package(
    @SerialName(value = "id")
    override var id: String = UUID.randomUUID().toString(),
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "version")
    val version: String,
    @SerialName(value = "publisher")
    val publisher: String,
    @SerialName(value = "s3_location")
    val s3location: String,
    //Spring uses JsonProperty, Elastic SerialName
    @JsonProperty(value = "upload_date")
    @SerialName(value = "upload_date")
    val uploadDate: String,
    @SerialName(value = "files")
    val files: List<String>,
    @SerialName(value = "dependencies")
    val dependencies: List<Dependency>?,
    @SerialName(value = "access_level")
    val accessLevel: AccessLevel,
    @SerialName(value = "stemcell")
    val stemcell: Stemcell?,
    @SerialName(value = "signed_with")
    val signedWith: String,
    @SerialName(value = "description")
    val description: String?,
    @SerialName(value = "size")
    var size: Long?,
    var url: String?

) : BaseEntity() {

  fun changeAccessLevel(accessLevel: AccessLevel): Package = Package(
      id = this.id,
      name = this.name,
      version = this.version,
      publisher = this.publisher,
      s3location = this.s3location,
      uploadDate = this.uploadDate,
      files = this.files,
      dependencies = this.dependencies,
      accessLevel = accessLevel,
      stemcell = this.stemcell,
      signedWith = this.signedWith,
      description = this.description,
      size = this.size,
      url = this.url
  )

  enum class AccessLevel {
    PRIVATE, PUBLISHER, PUBLIC;

    fun isAbove(accessLevel: AccessLevel): Boolean {
      return when (this) {
        PUBLIC -> false
        PUBLISHER -> accessLevel == PUBLIC
        PRIVATE -> accessLevel == PUBLISHER || accessLevel == PUBLIC
      }
    }
  }
}