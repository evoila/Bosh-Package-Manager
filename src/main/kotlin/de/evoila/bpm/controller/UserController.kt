package de.evoila.bpm.controller

import de.evoila.bpm.service.UserService
import org.keycloak.KeycloakPrincipal
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    val userService: UserService
) {

  @PutMapping(value = ["login"])
  fun loginUser(keycloakAuthenticationToken: KeycloakAuthenticationToken): ResponseEntity<Any> {
    val id = keycloakAuthenticationToken.name

    if (!userService.userExits(id)) {
      val keycloakPrincipal = keycloakAuthenticationToken.principal as KeycloakPrincipal<*>
      val email = keycloakPrincipal.keycloakSecurityContext.token.email
      userService.saveUser(id, email)
      log.info("User $id registered himself.")

      return ResponseEntity.status(HttpStatus.CREATED).body("First login. Saving email.")
    }

    log.info("User $id logged in")

    return ResponseEntity.ok().body("Welcome back!")
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(UserController::class.java)
  }
}