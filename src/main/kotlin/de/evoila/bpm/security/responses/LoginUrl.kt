package de.evoila.bpm.security.responses

data class LoginUrl(
    val host: String,
    val port: Int,
    val scheme: String,
    val path: String,
    val clientId: String,
    val redirectUri: String,
    val nonce: String,
    val state: String,
    val responseType: String,
    val responseMode: String,
    val scope: String,
    val kcIdpHint: String,
    val loginHint: String,
    val prompt: String
)