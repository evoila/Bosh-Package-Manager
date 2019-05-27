package de.evoila.bpm.controller

import de.evoila.bpm.config.S3Config
import de.evoila.bpm.entities.Package
import de.evoila.bpm.exceptions.PackageNotFoundException
import de.evoila.bpm.rest.bodies.PackageBody
import de.evoila.bpm.rest.bodies.S3Permission
import de.evoila.bpm.service.AmazonS3Service
import de.evoila.bpm.service.AmazonS3Service.Operation.*
import de.evoila.bpm.service.PackageService
import de.evoila.bpm.service.PublisherService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@CrossOrigin(origins = ["http://localhost:4200"])
@RestController
class CliClientPackageController(
    val packageService: PackageService,
    val publisherService: PublisherService,
    val s3Config: S3Config,
    val amazonS3Service: AmazonS3Service
) {

  @GetMapping(value = ["auth-test"])
  fun authTest(principal: Principal?): ResponseEntity<String> {

    log.info("Moin moin Auth")
    return principal?.let {
      log.info("${principal.name} is logged in!")

      ResponseEntity.ok("${principal.name} is logged in!")
    }
        ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Didn't work")
  }

  @GetMapping(value = ["packages/{id}"])
  fun getById(@PathVariable(value = "id") id: String): ResponseEntity<Any> {

    val result = packageService.findById(id)

    return result?.let { ResponseEntity.ok<Any>(it) } ?: ResponseEntity.notFound().build<Any>()
  }

  @PostMapping(value = ["upload/permission"])
  fun getUploadPermission(@RequestParam(value = "force") force: Boolean,
                          @RequestBody packageBody: PackageBody,
                          principal: Principal?
  ): ResponseEntity<Any> {
    val username = principal?.name ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body("Please log yourself in.")

    if (!publisherService.isMemberOf(username, packageBody.publisher)) {

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not a member of ${packageBody.publisher}")
    }

    if (packageBody.name.isEmpty() || packageBody.publisher.isEmpty() || packageBody.version.isEmpty()) {
      return ResponseEntity.badRequest().body("A package needs a name, version and a registered publisher.")
    }

    if (!force) {
      packageService.checkIfPresent(packageBody)?.let {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(it)
      }
    }

    val s3location = packageService.putPendingPackage(packageBody, principal.name)
    val uploadCredentials = amazonS3Service.getS3Credentials(UPLOAD)

    val uploadPermission = S3Permission(
        bucket = s3Config.bucket,
        region = s3Config.region,
        authKey = uploadCredentials.accessKeyId,
        authSecret = uploadCredentials.secretAccessKey,
        s3location = s3location,
        sessionToken = uploadCredentials.sessionToken
    )

    log.info("Granting permission to upload package '${packageBody.name}:${packageBody.version} by ${packageBody.publisher}'")

    return ResponseEntity.accepted().body(uploadPermission)
  }

  @PutMapping(value = ["package"])
  fun uploadPackage(@RequestParam("location") key: String, @RequestParam("size") size: Long): ResponseEntity<Any> = try {
    packageService.savePendingPackage(key, size)

    ResponseEntity.ok().build()
  } catch (e: PackageNotFoundException) {

    ResponseEntity.notFound().build()
  }

  @GetMapping(value = ["package"])
  fun getPackagesByName(@RequestParam(value = "name") name: String, principal: Principal?): ResponseEntity<Any> {

    val packages = packageService.getPackagesByName(principal?.name, name)

    return ResponseEntity.ok(packages)
  }

  @GetMapping(value = ["package/{publisher}/{name}"])
  fun getPackageByPublisherNameVersion(@PathVariable(value = "publisher") publisher: String,
                                       @PathVariable(value = "name") name: String,
                                       @RequestParam(value = "version") version: String,
                                       principal: Principal?
  ): ResponseEntity<Any> = try {
    val packageBody = packageService.accessPackage(publisher, name, version, principal?.name)
    log.info("Exposing package information for '$publisher:$name:$version'")

    ResponseEntity.ok(packageBody)
  } catch (e: PackageNotFoundException) {

    ResponseEntity.notFound().build()
  }

  @GetMapping(value = ["download/{publisher}/{name}/{version}"])
  fun downloadPermissionByPackageByPublisherNameVersion(@PathVariable(value = "publisher") publisher: String,
                                                        @PathVariable(value = "name") name: String,
                                                        @PathVariable(value = "version") version: String,
                                                        principal: Principal?
  ): ResponseEntity<Any> = try {
    val packageBody = packageService.accessPackage(publisher, name, version, principal?.name)
    val downloadCredentials = amazonS3Service.getS3Credentials(DOWNLOAD)
    val downloadPermission = S3Permission(
        bucket = s3Config.bucket,
        region = s3Config.region,
        authKey = downloadCredentials.accessKeyId,
        authSecret = downloadCredentials.secretAccessKey,
        s3location = packageBody.s3location,
        sessionToken = downloadCredentials.sessionToken
    )
    log.info("Created Permission to download package '$publisher:$name:$version'")

    ResponseEntity.ok(downloadPermission)
  } catch (e: PackageNotFoundException) {

    ResponseEntity.notFound().build()
  }

  @DeleteMapping(value = ["package/{publisher}/{name}/{version}"])
  fun deletePackageByPublisherNameVersion(
      @PathVariable(value = "publisher") publisher: String,
      @PathVariable(value = "name") name: String,
      @PathVariable(value = "version") version: String
  ): ResponseEntity<Any> = try {
    packageService.deletePackageIfAllowed(publisher, name, version)

    ResponseEntity.status(HttpStatus.ACCEPTED).build()
  } catch (e: PackageNotFoundException) {

    ResponseEntity.status(HttpStatus.NOT_FOUND).build()
  }

  @PatchMapping(value = ["publish/{id}"])
  fun publishPackage(
      @PathVariable(value = "id") id: String,
      @RequestParam(value = "access") access: String,
      principal: Principal
  ): ResponseEntity<Any> = try {
    val accessLevel = Package.AccessLevel.valueOf(access)
    packageService.alterAccessLevel(id, principal.name, accessLevel)

    ResponseEntity.ok().build()
  } catch (e: PackageNotFoundException) {

    ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
  }

  companion object {
    private val log = LoggerFactory.getLogger(CliClientPackageController::class.java)
  }
}