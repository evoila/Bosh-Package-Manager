package de.evoila.bpm

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling

@EnableConfigurationProperties
@SpringBootApplication
@EnableScheduling
class Application

fun main(args: Array<String>) {
  SpringApplication.run(Application::class.java, *args)
}
