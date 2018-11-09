package de.evoila.bpm.controller

import de.evoila.bpm.config.S3Config
import de.evoila.bpm.exceptions.PackageNotFoundException
import de.evoila.bpm.rest.bodies.PackageBody
import de.evoila.bpm.rest.bodies.S3Permission
import de.evoila.bpm.service.AmazonS3Service
import de.evoila.bpm.service.AmazonS3Service.Operation.*
import de.evoila.bpm.service.PackageService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class PackageController(
    val packageService: PackageService,
    val s3Config: S3Config,
    val amazonS3Service: AmazonS3Service
) {

  @GetMapping(value = ["upload/permission"])
  fun getUploadPermission(@RequestParam(value = "force") force: Boolean, @RequestBody packageBody: PackageBody): ResponseEntity<Any> {

    if (!force) {
      packageService.checkIfPresent(packageBody)?.let {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(it)
      }
    }

    val s3location = packageService.putPendingPackage(packageBody)
    val uploadCredentials = amazonS3Service.getS3Credentials(UPLOAD)

    val uploadPermission = S3Permission(
        bucket = s3Config.bucket,
        region = s3Config.region,
        authKey = uploadCredentials.accessKeyId,
        authSecret = uploadCredentials.secretAccessKey,
        s3location = s3location,
        sessionToken = uploadCredentials.sessionToken
    )

    log.info("Granting permission to upload package '${packageBody.name}:${packageBody.version} by ${packageBody.vendor}'")

    return ResponseEntity.accepted().body(uploadPermission)
  }

  @PutMapping(value = ["package"])
  fun uploadPackage(@RequestParam("location") key: String): ResponseEntity<Any> = try {
    packageService.savePendingPackage(key)

    ResponseEntity.ok().build()
  } catch (e: PackageNotFoundException) {

    ResponseEntity.notFound().build()
  }

  @GetMapping("package")
  fun getPackagesByName(@RequestParam(value = "name") name: String): ResponseEntity<Any> {

    val packages = packageService.getPackages(name)

    return ResponseEntity.ok(packages)
  }

  @GetMapping(value = ["package/{vendor}/{name}/{version}"])
  fun getPackageByVendorNameVersion(@PathVariable(value = "vendor") vendor: String,
                                    @PathVariable(value = "name") name: String,
                                    @PathVariable(value = "version") version: String
  ): ResponseEntity<Any> = try {
    val packageBody = packageService.getPackage(vendor, name, version)

    log.info("Exposing package information for '$name:$version by $vendor'")

    ResponseEntity.ok(packageBody)
  } catch (e: PackageNotFoundException) {
    ResponseEntity.notFound().build()
  }

  @GetMapping(value = ["download/{vendor}/{name}/{version}"])
  fun downloadPermissionByPackageByVendorNameVersion(@PathVariable(value = "vendor") vendor: String,
                                                     @PathVariable(value = "name") name: String,
                                                     @PathVariable(value = "version") version: String
  ): ResponseEntity<Any> = try {
    val packageBody = packageService.getPackage(vendor, name, version)

    val downloadCredentials = amazonS3Service.getS3Credentials(DOWNLOAD)

    val downloadPermission = S3Permission(
        bucket = s3Config.bucket,
        region = s3Config.region,
        authKey = downloadCredentials.accessKeyId,
        authSecret = downloadCredentials.secretAccessKey,
        s3location = packageBody.s3location,
        sessionToken = downloadCredentials.sessionToken
    )
    log.info("Created Permission to download package '$name:$version by $vendor'")

    ResponseEntity.ok(downloadPermission)
  } catch (e: PackageNotFoundException) {

    ResponseEntity.notFound().build()
  }

  companion object {
    private val log = LoggerFactory.getLogger(PackageController::class.java)
  }
}