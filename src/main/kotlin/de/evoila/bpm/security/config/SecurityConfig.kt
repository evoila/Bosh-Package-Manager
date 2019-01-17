package de.evoila.bpm.security.config

import org.keycloak.adapters.springsecurity.KeycloakConfiguration
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy

@KeycloakConfiguration
@EnableWebSecurity
class KeycloakSecurityConfig(
    val keycloakClientRequestFactory: KeycloakClientRequestFactory
) : KeycloakWebSecurityConfigurerAdapter() {

  override fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
    return NullAuthenticatedSessionStrategy()
  }

  override fun configure(http: HttpSecurity) {

    super.configure(http)
    http.cors()
        .and()
        .csrf()
        .disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
        .and()
        .authorizeRequests()
        .anyRequest().permitAll()
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  fun keycloakRestTemplate(): KeycloakRestTemplate {
    return KeycloakRestTemplate(keycloakClientRequestFactory)
  }

  @Bean
  fun keycloakAuthenticationProcessingFilterRegistrationBean(
      filter: KeycloakAuthenticationProcessingFilter): FilterRegistrationBean<*> {
    val registrationBean = FilterRegistrationBean(filter)
    registrationBean.isEnabled = false
    return registrationBean
  }

  @Bean
  fun keycloakPreAuthActionsFilterRegistrationBean(
      filter: KeycloakPreAuthActionsFilter): FilterRegistrationBean<*> {
    val registrationBean = FilterRegistrationBean(filter)
    registrationBean.isEnabled = false
    return registrationBean
  }

  @Autowired
  @Throws(Exception::class)
  fun configureGlobal(auth: AuthenticationManagerBuilder) {
    val keyCloakAuthProvider = keycloakAuthenticationProvider()
    keyCloakAuthProvider.setGrantedAuthoritiesMapper(SimpleAuthorityMapper())

    auth.authenticationProvider(keyCloakAuthProvider)
  }
}