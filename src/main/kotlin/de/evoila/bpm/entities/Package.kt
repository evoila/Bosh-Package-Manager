package de.evoila.bpm.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Package(
    @SerialName(value = "id")
    override var id: String = UUID.randomUUID().toString(),
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "name_keyword")
    val nameKeyword: String = name,
    @SerialName(value = "version")
    val version: String,
    @SerialName(value = "version_keyword")
    val versionKeyword: String = version,
    @SerialName(value = "vendor")
    val vendor: String,
    @SerialName(value = "vendor_keyword")
    val vendorKeyword: String = vendor,
    @SerialName(value = "s3location")
    val s3location: String,
    @SerialName(value = "s3location_keyword")
    val s3locationKeyword: String = s3location,
    @SerialName(value = "upload_date")
    val uploadDate: String,
    @SerialName(value = "files")
    val files: List<String>,
    @SerialName(value = "dependencies")
    val dependencies: List<Dependency>?,
    @SerialName(value = "access_level")
    val accessLevel: AccessLevel,
    @SerialName(value = "access_level_keyword")
    val accessLevelKeyword: AccessLevel = accessLevel,
    @SerialName(value = "stemcell")
    val stemcell: Stemcell?,
    @SerialName(value = "signed_with")
    val signedWith: String,
    @SerialName(value = "signed_with_keyword")
    val signedWithKeyword: String = signedWith,
    @SerialName(value = "description")
    val description: String?
) : BaseEntity() {

  fun changeAccessLevel(accessLevel: AccessLevel): Package = Package(
      id = this.id,
      name = this.name,
      version = this.version,
      vendor = this.vendor,
      s3location = this.s3location,
      uploadDate = this.uploadDate,
      files = this.files,
      dependencies = this.dependencies,
      accessLevel = accessLevel,
      stemcell = this.stemcell,
      signedWith = this.signedWith,
      description = this.description
  )

  enum class AccessLevel {
    PRIVATE, VENDOR, PUBLIC;

    fun isAbove(accessLevel: AccessLevel): Boolean {
      return when (this) {
        PUBLIC -> false
        VENDOR -> accessLevel == PUBLIC
        PRIVATE -> accessLevel == VENDOR || accessLevel == PUBLIC
      }
    }
  }
}