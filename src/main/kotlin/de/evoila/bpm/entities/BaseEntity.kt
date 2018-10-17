package de.evoila.bpm.entities

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import java.io.Serializable
import java.util.*


abstract class BaseEntity : Serializable {

  @Id
  @JsonProperty(value = "id")
  var id: String = UUID.randomUUID().toString()
}