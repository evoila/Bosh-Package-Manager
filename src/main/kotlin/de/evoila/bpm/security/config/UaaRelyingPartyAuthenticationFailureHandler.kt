package de.evoila.bpm.security.config

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class UaaRelyingPartyAuthenticationFailureHandler : AuthenticationFailureHandler {

  @Throws(IOException::class, ServletException::class)
  override fun onAuthenticationFailure(request: HttpServletRequest, response: HttpServletResponse,
                                       exception: AuthenticationException) {

    response.addHeader("Access-Control-Expose-Headers", "WWW-Authenticate, Access-Control-Allow-Origin")
    response.addHeader("Access-Control-Allow-Origin", "*")

    response.addHeader("WWW-Authenticate", "MeshFed realm=${request.scheme}://${request.serverName}:${request.serverPort}/login")

    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.message)
  }

}
