package de.evoila.bpm.security.controller

import de.evoila.bpm.security.config.AuthConfig
import de.evoila.bpm.security.responses.LoginUrl
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@CrossOrigin(origins = ["http://localhost:4200"], maxAge = 3600)
@RestController
class AuthenticationController(
    val authConfig: AuthConfig
) {

  @PostMapping("/login")
  fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {

    return ResponseEntity.ok(getLoginUrl(loginRequest))
  }

  protected fun getLoginUrl(login: LoginRequest) =
      LoginUrl(
          scheme = authConfig.scheme,
          host = authConfig.host,
          port = authConfig.port,
          path = authConfig.loginPath,
          clientId = authConfig.clientId,
          responseType = authConfig.responseType,
          responseMode = authConfig.responseMode,
          scope = authConfig.scope,
          redirectUri = login.redirectUri,
          nonce = login.nonce,
          loginHint = login.email,
          prompt = login.promt,
          state = login.state,
          kcIdpHint = login.kcIdpHint
      )

  class LoginRequest(
      val email: String,
      val redirectUri: String,
      val promt: String,
      val nonce: String,
      val state: String,
      val kcIdpHint: String
  )
}