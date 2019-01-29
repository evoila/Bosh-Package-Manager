package de.evoila.bpm.controller

import de.evoila.bpm.service.UserService
import org.keycloak.KeycloakPrincipal
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    val userService: UserService
) {

  @PutMapping(value = ["register"])
  fun registerUser(keycloakAuthenticationToken: KeycloakAuthenticationToken): ResponseEntity<Any> {
    val id = keycloakAuthenticationToken.name
    val keycloakPrincipal = keycloakAuthenticationToken.principal as KeycloakPrincipal<*>
    val email = keycloakPrincipal.keycloakSecurityContext.token.email

    userService.saveUser(id, email)

    return ResponseEntity.ok().build()
  }
}