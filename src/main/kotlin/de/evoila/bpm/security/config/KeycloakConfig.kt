package de.evoila.bpm.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "keycloak")
class KeycloakConfig {

  lateinit var endpoint: String
  lateinit var authRealm: String
  lateinit var realm: String
  lateinit var username: String
  lateinit var password: String
  lateinit var clientId: String
}