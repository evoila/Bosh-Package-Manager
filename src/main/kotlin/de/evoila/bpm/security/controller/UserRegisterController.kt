package de.evoila.bpm.security.controller

import de.evoila.bpm.security.exceptions.UserExistsException
import de.evoila.bpm.security.model.User
import de.evoila.bpm.security.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserRegisterController(
    val userService: UserService
) {

  @PostMapping("/register")
  fun register(@RequestBody user: User): ResponseEntity<Any> {

    return try {
      userService.addNewUserIfUnusedData(user)

      ResponseEntity.status(HttpStatus.CREATED).build()

    } catch (e: UserExistsException) {

      ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
    }
  }
}