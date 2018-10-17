package de.evoila.bpm.rest.bodies

data class PackageBody(
    val name: String,
    val version: String,
    val blobs: List<BlobBody>,
    val dependencies: List<Dependency>,
    val spec: String?,
    val packaging: String?
) {

  data class Dependency(
      val name: String,
      val version: String
  )
}