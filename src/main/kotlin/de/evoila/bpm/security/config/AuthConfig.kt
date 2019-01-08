package de.evoila.bpm.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "auth.openid")
class AuthConfig {

  lateinit var scheme: String
  lateinit var host: String
  var port: Int = 80
  lateinit var loginPath: String
  lateinit var logoutPath: String
  lateinit var clientId: String
  lateinit var responseType: String
  lateinit var responseMode: String
  lateinit var scope: String
}