package de.evoila.bpm.rest.bodies

import com.fasterxml.jackson.annotation.JsonProperty

data class S3Permission(
    @JsonProperty(value = "bucket")
    val bucket: String,
    @JsonProperty(value = "region")
    val region: String,
    @JsonProperty(value = "auth-key")
    val authKey: String,
    @JsonProperty(value = "auth-secret")
    val authSecret: String,
    @JsonProperty(value = "s3location")
    val s3location: String,
    @JsonProperty(value = "session-token")
    val sessionToken: String
)