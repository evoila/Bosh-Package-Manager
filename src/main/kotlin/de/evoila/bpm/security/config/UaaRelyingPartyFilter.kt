package de.evoila.bpm.security.config

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.util.Assert
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class UaaRelyingPartyFilter(authenticationManager: AuthenticationManager) : GenericFilterBean() {

  private var authenticationManager: AuthenticationManager? = null

  private var requiresAuthenticationRequestMatcher: RequestMatcher? = null

  private val successHandler: AuthenticationSuccessHandler = UaaRelyingPartyAuthenticationSuccessHandler()
  var failureHandler: AuthenticationFailureHandler = UaaRelyingPartyAuthenticationFailureHandler()

  init {
    setFilterProcessesUrl("/v2/manage/**")
    this.setAuthenticationManager(authenticationManager)
  }

  private fun setAuthenticationManager(authenticationManager: AuthenticationManager) {
    this.authenticationManager = authenticationManager
  }

  private fun setFilterProcessesUrl(filterProcessesUrl: String) {
    setRequiresAuthenticationRequestMatcher(AntPathRequestMatcher(filterProcessesUrl))
  }

  private fun setRequiresAuthenticationRequestMatcher(requestMatcher: RequestMatcher) {
    Assert.notNull(requestMatcher, "requestMatcher cannot be null")
    this.requiresAuthenticationRequestMatcher = requestMatcher
  }

  protected fun requiresAuthentication(
      request: HttpServletRequest, response: HttpServletResponse): Boolean {
    return requiresAuthenticationRequestMatcher!!.matches(request)
  }

  @Throws(IOException::class, ServletException::class)
  override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
    val request = req as HttpServletRequest
    val response = res as HttpServletResponse

    val isOption = request.method == HttpMethod.OPTIONS.toString()
    if (isOption || !requiresAuthentication(request, response)) {
      chain.doFilter(request, response)
      return
    }

    // We need to handle the tokens here, check the implementation how to resovle it
    val token = UaaFilterUtils.tryResolveToken(request, HttpHeaders.AUTHORIZATION)
    try {

      if (token == null) {
        throw AuthenticationCredentialsNotFoundException("No authorization header present.")
      }

      val authResult = this.authenticationManager!!.authenticate(UaaRelyingPartyToken(token, null, null))
      if (authResult != null) {
        successfulAuthentication(request, response, chain, authResult)
        return
      }

    } catch (ex: InternalAuthenticationServiceException) {
      logger.error("An internal error occurred while trying to authenticate the user.", ex)
      unsuccessfulAuthentication(request, response, chain, ex)
    } catch (ex: AuthenticationException) {
      unsuccessfulAuthentication(request, response, chain, ex)
    }

  }

  @Throws(IOException::class, ServletException::class)
  private fun unsuccessfulAuthentication(
      request: HttpServletRequest,
      response: HttpServletResponse,
      chain: FilterChain,
      failed: AuthenticationException) {
    SecurityContextHolder.clearContext()

    if (logger.isDebugEnabled) {
      logger.debug("Authentication request failed: " + failed.toString())
      logger.debug("Updated SecurityContextHolder to contain null Authentication")
      logger.debug("Delegating to authentication failure handler $failureHandler")
    }
    failureHandler.onAuthenticationFailure(request, response, failed)
  }

  @Throws(IOException::class, ServletException::class)
  private fun successfulAuthentication(
      request: HttpServletRequest,
      response: HttpServletResponse,
      chain: FilterChain,
      authResult: Authentication) {

    if (logger.isDebugEnabled) {
      logger.debug(
          "Authentication success. Updating SecurityContextHolder to contain: $authResult")
    }

    SecurityContextHolder.getContext().authentication = authResult

    successHandler.onAuthenticationSuccess(request, response, authResult)

    chain.doFilter(request, response)
  }
}
