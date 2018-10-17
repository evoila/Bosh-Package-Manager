package de.evoila.bpm.controller

import de.evoila.bpm.rest.bodies.BlobBody
import de.evoila.bpm.rest.bodies.PackageBody
import de.evoila.bpm.service.BlobService
import de.evoila.bpm.service.PackageService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import javax.servlet.http.HttpServletResponse

@RestController
class PackageController(
    val packageService: PackageService,
    val blobService: BlobService
) {

  @PutMapping(value = ["upload/package"])
  fun uploadPackage(@RequestBody packageBody: PackageBody): ResponseEntity<Any> {

    val saved = packageService.saveRelease(packageBody)

    return ResponseEntity.accepted().body(saved)
  }

  @GetMapping(value = ["package/{uuid}"])
  fun getAllPackagesForId(@PathVariable(value = "uuid") uuid: String): ResponseEntity<Any> {

    val packages = packageService.getPackageWithDependenciesAsList(uuid)

    return ResponseEntity.ok().body(packages)
  }

  @PutMapping(value = ["upload/blob"])
  fun uploadBlob(@RequestParam(value = "name") name: String,
                 @RequestParam(value = "version") version: String,
                 @RequestParam(value = "type") type: String,
                 @RequestParam(value = "blob") blob: MultipartFile): ResponseEntity<Any> {

    val blobBody = BlobBody(
        name = name,
        version = version
    )

    return ResponseEntity.accepted().body(blobService.storeBlob(blobBody, type, blob))
  }

  @GetMapping(value = ["blob/{uuid}"])
  fun downloadBlob(response: HttpServletResponse,
                   @PathVariable(value = "uuid") uuid: String,
                   @Param(value = "filename") filename: String) {

    try {
      val inputStream = blobService.findBlobFile(uuid)

      response.setHeader("Content-Disposition", "attachment; filename=$filename")

      val outputStream = response.outputStream
      val buffer = ByteArray(BUFFER_SIZE)
      var bytesRead = inputStream.read(buffer)

      while (bytesRead != -1) {

        outputStream.write(buffer, 0, bytesRead)
        bytesRead = inputStream.read(buffer)
      }

      inputStream.close()
      outputStream.close()

    } catch (e: IOException) {
      log.error(e.message)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(PackageController::class.java)
    private const val BUFFER_SIZE = 4096
  }
}