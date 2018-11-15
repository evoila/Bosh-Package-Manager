package de.evoila.bpm.security.controller

import de.evoila.bpm.security.exceptions.VendorException
import de.evoila.bpm.security.model.User
import de.evoila.bpm.security.model.Vendor
import de.evoila.bpm.security.service.VendorService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class VendorController(
    val vendorService: VendorService
) {

  @PostMapping("/vendor")
  fun newVendor(@RequestBody vendor: Vendor): ResponseEntity<Any> = try {

    val user: User = SecurityContextHolder.getContext().authentication.principal as User
    vendorService.addVendorIfNew(vendor, user)

    ResponseEntity.status(HttpStatus.CREATED).build()

  } catch (e: VendorException) {

    log.error(e.message)
    ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
  }

  @PatchMapping("/vendor/newMember")
  fun addVendorMember(@RequestBody addMemberBody: AddMemberBody): ResponseEntity<Any> = try {

    vendorService.addMemberToVendor(addMemberBody.vendor, addMemberBody.username)

    ResponseEntity.status(HttpStatus.ACCEPTED).build()

  } catch (e: Exception) {

    log.error(e.message)
    ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
  }

  data class AddMemberBody(
      val username: String,
      val vendor: String
  )

  companion object {
    val log: Logger = LoggerFactory.getLogger(VendorController::class.java)
  }
}