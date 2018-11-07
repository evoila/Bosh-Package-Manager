package de.evoila.bpm.config


import com.amazonaws.regions.Region
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(value = "s3")
class S3Config {
  lateinit var authKey: String
  lateinit var authSecret: String
  lateinit var bucket: String
  lateinit var region: String

}