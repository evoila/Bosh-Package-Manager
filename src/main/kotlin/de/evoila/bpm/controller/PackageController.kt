package de.evoila.bpm.controller

import de.evoila.bpm.config.S3Config
import de.evoila.bpm.exceptions.PackageNotFoundException
import de.evoila.bpm.rest.bodies.PackageBody
import de.evoila.bpm.rest.bodies.S3Permission
import de.evoila.bpm.service.PackageService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class PackageController(
    val packageService: PackageService,
    val s3Config: S3Config
) {

  @PutMapping(value = ["upload/package"])
  fun uploadPackage(@RequestBody packageBody: PackageBody): ResponseEntity<Any> {

    val saved = packageService.save(packageBody)

    val uploadPermission = S3Permission(
        bucket = s3Config.bucket,
        region = s3Config.region,
        authKey = s3Config.authKey,
        authSecret = s3Config.authSecret,
        s3location = saved.s3location
    )

    log.info("Saved package ${saved.name}:${saved.version} by ${saved.vendor}")

    return ResponseEntity.accepted().body(uploadPermission)
  }

  @GetMapping(value = ["{vendor}/{name}/{version}"])
  fun getAllPackagesForId(@PathVariable(value = "vendor") vendor: String,
                          @PathVariable(value = "name") name: String,
                          @PathVariable(value = "version") version: String): ResponseEntity<Any> {

    return try {
      val packageBody = packageService.getPackage(vendor, name, version)

      val downloadPermission = S3Permission(
          bucket = s3Config.bucket,
          region = s3Config.region,
          authKey = s3Config.authKey,
          authSecret = s3Config.authSecret,
          s3location = packageBody.s3location
      )

      ResponseEntity.ok(downloadPermission)
    } catch (e: PackageNotFoundException) {

      ResponseEntity.notFound().build()
    }
  }

  @DeleteMapping(value = ["package/{uuid}"])
  fun deletePackageById() {

    //   packageService.deletePackage()

  }

  companion object {
    private val log = LoggerFactory.getLogger(PackageController::class.java)
  }
}