package de.evoila.bpm.controller

import de.evoila.bpm.exceptions.UnauthorizedException
import de.evoila.bpm.service.VendorService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
class VendorController(
    val vendorService: VendorService) {

  @PostMapping(value = ["vendors"])
  fun createNewVendor(
      @RequestParam("name") name: String,
      principal: Principal
  ): ResponseEntity<Any> = try {
    vendorService.createNewVendor(name, principal.name)

    ResponseEntity.status(HttpStatus.CREATED).build()
  } catch (e: Exception) {
    e.printStackTrace()

    ResponseEntity.status(HttpStatus.CONFLICT).build()
  }

  @PatchMapping(value = ["vendors/add-member"])
  fun addNewMember(@RequestParam(value = "vendor") vendor: String,
                   @RequestParam(value = "email") email: String,
                   principal: Principal
  ): ResponseEntity<Any> = try {
    vendorService.addMemberToVendor(
        admin = principal.name,
        vendorName = vendor,
        email = email
    )

    ResponseEntity.status(HttpStatus.ACCEPTED).body("Added $email to $vendor")
  } catch (e: UnauthorizedException) {
    log.error(e.message, e)

    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.message)
  } catch (e: Exception) {
    log.error(e.message, e)

    ResponseEntity.badRequest().body(e.message)
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(VendorController::class.java)
  }
}