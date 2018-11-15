package de.evoila.bpm.security.controller

import de.evoila.bpm.security.exceptions.UserExistsException
import de.evoila.bpm.security.exceptions.VendorExistsException
import de.evoila.bpm.security.model.User
import de.evoila.bpm.security.model.UserRole
import de.evoila.bpm.security.model.Vendor
import de.evoila.bpm.security.service.UserService
import de.evoila.bpm.security.service.VendorService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserRegisterController(
    val userService: UserService,
    val vendorService: VendorService
) {

  @PostMapping("/register")
  fun register(@RequestBody user: RegisterBody): ResponseEntity<Any> {

    return try {
      userService.addNewUserIfUnusedData(user)

      ResponseEntity.status(HttpStatus.CREATED).build()

    } catch (e: UserExistsException) {

      ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
    }
  }

  @PostMapping("/vendor")
  fun newVendor(@RequestBody vendor: Vendor): ResponseEntity<Any> = try {

    val user: User = SecurityContextHolder.getContext().authentication.principal as User
    vendorService.addVendorIfNew(vendor, user)

    ResponseEntity.status(HttpStatus.CREATED).build()

  } catch (e: VendorExistsException) {

    ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
  }

  @PatchMapping("/vendor/newMember")
  fun addVendorMember(@RequestBody addMemberBody: AddMemberBody): ResponseEntity<Any> = try {

    vendorService.addMemberToVendor(addMemberBody.vendor, addMemberBody.username)

    ResponseEntity.status(HttpStatus.ACCEPTED).build()

  } catch (e: Exception) {

    ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
  }

  data class AddMemberBody(
      val username: String,
      val vendor: String
  )

  data class RegisterBody(
      val username: String,
      val email: String,
      val password: String
  )
}