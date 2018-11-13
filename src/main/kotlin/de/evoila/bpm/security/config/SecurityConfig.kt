package de.evoila.bpm.security.config

import de.evoila.bpm.security.model.UserRole.Role.*
import de.evoila.bpm.security.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
@EnableWebSecurity
class SecurityConfig(
    val userService: UserService
) : WebSecurityConfigurerAdapter() {

  override fun configure(auth: AuthenticationManagerBuilder) {
    auth
        .userDetailsService(userService).passwordEncoder(getPasswordEncoder())
        .and()
        .inMemoryAuthentication()
        .withUser("test").password(getPasswordEncoder().encode("test")).roles(ADMIN.name)
        .and()
        .withUser("guest").password(getPasswordEncoder().encode("guest")).roles("GUEST")
  }

  override fun configure(http: HttpSecurity) {
    http.httpBasic().and()
        .authorizeRequests()
        .antMatchers(UPLOAD_PATH).hasAnyRole(ADMIN.name, VENDOR.name)
        .antMatchers(HttpMethod.DELETE, *PACKAGE_PATH).hasRole(ADMIN.name)
        .antMatchers(HttpMethod.PUT, *PACKAGE_PATH).hasRole(ADMIN.name)
        .antMatchers(HttpMethod.PATCH, *PACKAGE_PATH).hasRole(ADMIN.name)
        .antMatchers(HttpMethod.PUT, *VENDOR_PATH).hasRole(GUEST.name)
        .antMatchers(HttpMethod.PATCH, *VENDOR_PATH).hasRole(GUEST.name)
        .antMatchers(HttpMethod.DELETE, *VENDOR_PATH).hasRole(GUEST.name)
        .and().csrf().disable()
  }

  @Bean
  fun getPasswordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

  companion object {
    private const val UPLOAD_PATH = "/upload/"
    private val PACKAGE_PATH = arrayOf("/rest/packages", "/rest/packages/**")
    private val VENDOR_PATH = arrayOf("rest/vendors", "rest/vendors/*")
  }
}