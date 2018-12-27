package de.evoila.bpm.security.config

import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class UaaRelyingPartyAuthenticationSuccessHandler : AuthenticationSuccessHandler {

  @Throws(ServletException::class, IOException::class)
  override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse,
                                       authentication: Authentication) {
  }
}