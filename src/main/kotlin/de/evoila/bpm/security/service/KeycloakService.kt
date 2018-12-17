package de.evoila.bpm.security.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.bpm.security.config.KeycloakConfig
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.IdentityProviderRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*

@Service
class KeycloakService(
    private val keycloakConfig: KeycloakConfig
) {

  private final val keycloak: Keycloak
  var objectMapper: ObjectMapper = ObjectMapper()

  init {

    val resteasyClient = ResteasyClientBuilder()
        .disableTrustManager()
        .connectionPoolSize(10)
        .build()

    this.keycloak = KeycloakBuilder.builder()
        .serverUrl(keycloakConfig.endpoint)
        .realm(keycloakConfig.authRealm)
        .username(keycloakConfig.username)
        .password(keycloakConfig.password)
        .clientId(keycloakConfig.clientId)
        .resteasyClient(resteasyClient)
        .build()

  }

  fun createUser(username: String, email: String,
                 credentialPassword: String? = null): Boolean {
    var user = UserRepresentation()
    user.username = username
    user.email = email
    user.isEnabled = true

    val attribute = ArrayList<String>()

    val response = keycloak.realm(keycloakConfig.realm)
        .users()
        .create(user)

    val users = this.searchUser(user.email)
    if (response.status == 201 && users.size == 1) {
      user = users[0]

      if (user.attributes == null) {
        user.attributes = HashMap()
      }

      this.updateUser(user)

      credentialPassword?.let {
        if (!credentialPassword.isNotEmpty()) {
          keycloak.realm(keycloakConfig.realm)
              .users()
              .get(user.id)
              .resetPassword(createCredentials(credentialPassword, false))
        }
      }

      log.info("User successfully created", response.entity)
      return true
    }
    log.info("Could not create User", response.entity)
    return false
  }

  fun createUser(user: UserRepresentation): Boolean {

    val response = keycloak.realm(keycloakConfig.realm).users().create(user)

    if (response.status != 201) {
      log.info("Could not create User", response.entity)

      return false
    }

    val users = this.searchUser(user.email)

    if (users.size == 1) {
      log.info("User successfully created", response.entity)

      return true
    }

    log.info("Could not create User", response.entity)

    return false
  }

  fun searchUser(searchParams: String): List<UserRepresentation> {
    return keycloak.realm(keycloakConfig.realm)
        .users()
        .search(searchParams, 0, 1)
  }

  fun userExists(email: String): Boolean {
    if (email.isEmpty()) {
      return false
    }

    val list = searchUser(email)

    return list.isNotEmpty()
  }

  fun updateUser(user: UserRepresentation) {
    keycloak.realm(keycloakConfig.realm)
        .users()
        .get(user.id)
        .update(user)
  }

  fun removeUser(email: String) {
    val users = this.searchUser(email)

    if (users.size != 1) {
      return
    }

    removeUser(users[0])
  }

  fun removeUser(user: UserRepresentation) {
    keycloak.realm(keycloakConfig.realm)
        .users()
        .get(user.id)
        .remove()
  }

  fun resetPassword(email: String, password: String, isTemporary: Boolean = true) {
    val users = this.searchUser(email)

    if (users.size != 1) {
      return
    }

    resetPassword(users[0], password, isTemporary)
  }

  fun resetPassword(user: UserRepresentation, password: String, isTemporary: Boolean = true) {
    val credentialRepresentation = this.createCredentials(password, isTemporary)

    keycloak.realm(keycloakConfig.realm)
        .users()
        .get(user.id)
        .resetPassword(credentialRepresentation)
  }

  fun createCredentials(credentialPassword: String, isTemporary: Boolean): CredentialRepresentation {
    val credential = CredentialRepresentation()
    credential.type = CredentialRepresentation.PASSWORD
    credential.value = credentialPassword
    credential.isTemporary = isTemporary

    return credential
  }

  fun assignCustomerDomain(user: UserRepresentation, customerName: String) {
    val value = ArrayList<String>()
    value.add(customerName)

    user.attributes[CUSTOMER_DOMAIN] = value
  }

  fun updateEntitledGroups(user: UserRepresentation, groupName: String) {
    val groups: MutableList<String>
    try {
      if (user.attributes == null) {
        user.attributes = HashMap()
      }

      groups = this.decodeGroupAttribute(user.attributes)


      if (groups.indexOf(groupName) == -1) {
        groups.add(groupName)
      }

      val encodedGroups = this.encodeGroupAttribute(groups)

      user.attributes[KeycloakService.ENTITLED_GROUPS] = encodedGroups
    } catch (e: IOException) {
      e.printStackTrace()
    }

  }

  @Throws(IOException::class)
  private fun decodeGroupAttribute(attributes: Map<String, List<String>>): MutableList<String> {

    if (attributes.containsKey(ENTITLED_GROUPS)) {
      val groupAttributes = attributes[ENTITLED_GROUPS]!!.toMutableList()

      if (groupAttributes.size == 1 && groupAttributes[0].isNotEmpty()) {
        return objectMapper.readValue<MutableList<String>>(groupAttributes[0], object : TypeReference<MutableList<String>>() {

        })
      }
    }

    return emptyList<String>().toMutableList()
  }

  @Throws(JsonProcessingException::class)
  private fun encodeGroupAttribute(decodedGroups: List<String>): List<String> {

    val groups = ArrayList<String>()
    val groupString = objectMapper.writeValueAsString(decodedGroups)
    groups.add(groupString)

    return groups
  }

  fun createIdentityProvider() {
    val identityProviderRepresentation = IdentityProviderRepresentation()

    keycloak.realm(keycloakConfig.realm)
        .identityProviders()
        .create(identityProviderRepresentation)
  }

  companion object {
    private const val ENTITLED_GROUPS = "MC_ENTITLED_GROUPS"
    private const val CUSTOMER_DOMAIN = "MC_CUSTOMER_DOMAIN"

    private val log = LoggerFactory.getLogger(KeycloakService::class.java)
  }
}