package de.evoila.bpm.controller

import de.evoila.bpm.service.VendorService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class VendorController(
    val vendorService: VendorService
) {

  @PostMapping("vendors")
  fun createNewVendor(
      @RequestParam("name") name: String,
      principal: Principal): ResponseEntity<Any> {

    try {
      vendorService.createNewVendor(name, principal.name)
    } catch (e: Exception) {
      e.printStackTrace()
      return ResponseEntity.status(HttpStatus.CONFLICT).build()
    }

    return ResponseEntity.status(HttpStatus.CREATED).build()
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(VendorController::class.java)
  }
}