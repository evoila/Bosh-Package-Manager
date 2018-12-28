package de.evoila.bpm.security.controller

import de.evoila.bpm.security.exceptions.UserException
import de.evoila.bpm.security.service.KeycloakService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@CrossOrigin(origins = ["http://localhost:4200"], maxAge = 3600)
@RestController
class UserController(
    val keycloakService: KeycloakService
) {

  @PostMapping("/register")
  fun register(@RequestBody user: RegisterBody): ResponseEntity<Any> {

    return try {

      ResponseEntity.status(HttpStatus.CREATED).build()

    } catch (e: UserException) {

      ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
    }
  }

  data class RegisterBody(
      val username: String,
      val email: String,
      val password: String
  )

  companion object {

    val log: Logger = LoggerFactory.getLogger(UserController::class.java)
  }
}