package de.evoila.bpm.security.config

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CommonCorsAuthenticationEntryPoint : AuthenticationEntryPoint {

  @Throws(IOException::class, ServletException::class)
  override fun commence(request: HttpServletRequest, response: HttpServletResponse,
                        exception: AuthenticationException) {

    response.addHeader("Access-Control-Allow-Origin", "*")
    response.sendError(HttpServletResponse.SC_FORBIDDEN, exception.message)
  }

}
