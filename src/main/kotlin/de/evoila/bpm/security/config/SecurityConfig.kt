package de.evoila.bpm.security.config

import de.evoila.bpm.security.model.UserRole.Role.*
import de.evoila.bpm.security.service.UserService
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.beans.factory.annotation.Autowired


@Configuration
@EnableWebSecurity
class SecurityConfig(
    val userService: UserService
) : WebSecurityConfigurerAdapter() {

  override fun configure(http: HttpSecurity) {
    http.httpBasic().and()
        .authorizeRequests()
        .antMatchers(UPLOAD_PATH).hasAnyAuthority(ADMIN.name, VENDOR.name)
        .antMatchers(HttpMethod.DELETE, *PACKAGE_PATH).hasAuthority(ADMIN.name)
        .antMatchers(HttpMethod.PUT, *PACKAGE_PATH).hasAuthority(ADMIN.name)
        .antMatchers(HttpMethod.PATCH, *PACKAGE_PATH).hasAuthority(ADMIN.name)
        .antMatchers(HttpMethod.PUT, *VENDOR_PATH).hasAuthority(GUEST.name)
        .antMatchers(HttpMethod.POST, *VENDOR_PATH).hasAuthority(GUEST.name)
        .antMatchers(HttpMethod.PATCH, *VENDOR_PATH).hasAuthority(GUEST.name)
        .antMatchers(HttpMethod.DELETE, *VENDOR_PATH).hasAuthority(GUEST.name)
        .antMatchers("/publish/**").hasAnyAuthority(VENDOR.name)
        .and().csrf().disable()
  }

  @Autowired
  fun configureGlobal(auth: AuthenticationManagerBuilder) {
    auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder())
  }

  companion object {

    private const val UPLOAD_PATH = "/upload/"
    private val PACKAGE_PATH = arrayOf("/rest/packages", "/rest/packages/**")
    private val VENDOR_PATH = arrayOf("/rest/vendors", "/rest/vendors/*")
  }
}

fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
  return BCryptPasswordEncoder()
}
