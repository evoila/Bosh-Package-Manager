package de.evoila.bpm.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "elastic-search")
class ElasticSearchConfig {

  lateinit var clusterName: String
  lateinit var host: String
  var port: Int = 9300
  var scheme: String = "http"
  var username: String? = null
  var password: String? = null

}