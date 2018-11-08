package de.evoila.bpm.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(value = "s3")
class S3Config {
  lateinit var bucket: String
  lateinit var region: String

  val creds: HashMap<String, Creds> = hashMapOf()
}

class Creds {
  lateinit var authKey: String
  lateinit var authSecret: String
}
