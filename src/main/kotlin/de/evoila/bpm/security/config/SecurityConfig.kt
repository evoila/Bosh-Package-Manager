package de.evoila.bpm.security.config

import de.evoila.bpm.security.model.UserRole.Role.*
import org.keycloak.adapters.springsecurity.KeycloakConfiguration
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter


//
//@Configuration
//@EnableWebSecurity
//@Order(1)
//class SecurityConfig(
//    val userService: UserService
//) : WebSecurityConfigurerAdapter() {
//
//
//  override fun configure(http: HttpSecurity) {
//    http.httpBasic().and()
//        .authorizeRequests()
//        .antMatchers(Path.UPLOAD).hasAnyAuthority(ADMIN.name, VENDOR.name)
//        .antMatchers(HttpMethod.DELETE, *Path.PACKAGE).hasAuthority(ADMIN.name)
//        .antMatchers(HttpMethod.PUT, *Path.PACKAGE).hasAuthority(ADMIN.name)
//        .antMatchers(HttpMethod.PATCH, *Path.PACKAGE).hasAuthority(ADMIN.name)
//        .antMatchers(HttpMethod.PUT, *Path.VENDOR).hasAuthority(GUEST.name)
//        .antMatchers(HttpMethod.POST, *Path.VENDOR).hasAuthority(GUEST.name)
//        .antMatchers(HttpMethod.PATCH, *Path.VENDOR).hasAuthority(GUEST.name)
//        .antMatchers(HttpMethod.DELETE, *Path.VENDOR).hasAuthority(GUEST.name)
//        .antMatchers("/publish/**").hasAnyAuthority(VENDOR.name)
//        .and().csrf().disable()
//  }
//
//  @Autowired
//  fun configureGlobal(auth: AuthenticationManagerBuilder) {
//    auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder())
//  }
//
//}
//
//fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
//  return BCryptPasswordEncoder()
//}


@KeycloakConfiguration
@EnableWebSecurity
@Order(10)
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
        .antMatchers(HttpMethod.GET, *Path.PACKAGE).hasAuthority(GUEST.name)
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
}

object Path {
  const val UPLOAD = "/upload/"
  val PACKAGE = arrayOf("/rest/packages", "/rest/packages/**")
  val VENDOR = arrayOf("/rest/vendors", "/rest/vendors/*")
}