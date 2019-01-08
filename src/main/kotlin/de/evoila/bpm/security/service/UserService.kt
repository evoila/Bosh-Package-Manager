package de.evoila.bpm.security.service

/*

import de.evoila.bpm.security.config.bCryptPasswordEncoder
import de.evoila.bpm.security.controller.UserController
import de.evoila.bpm.security.exceptions.UserException
import de.evoila.bpm.security.model.User
import de.evoila.bpm.security.model.UserRole
import de.evoila.bpm.security.repositories.RoleRepository
import de.evoila.bpm.security.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*


@Service
class UserService(
    val keycloakService: KeycloakService,
    val userRepository: UserRepository,
    val roleRepository: RoleRepository
) : UserDetailsService {

  override fun loadUserByUsername(username: String): UserDetails {

    return userRepository.findByUsername(username)
        ?: throw UsernameNotFoundException("Did not find user $username")
  }

  fun addNewUserIfUnusedData(user: UserController.RegisterBody) {

    if (userRepository.existsByEmail(user.email)) {
      throw UserException("Email '${user.email}' already in use.")
    }
    if (userRepository.existsByUsername(user.username)) {
      throw UserException("User '${user.username}' already exists.")
    }

    keycloakService.createUser(username = user.username,
        email = user.email,
        credentialPassword = user.password
    )

    val encodedUser = User(
        username = user.username,
        email = user.email,
        password = bCryptPasswordEncoder().encode(user.password),
        signingKey = UUID.randomUUID().toString()
    )

    userRepository.save(encodedUser)

    roleRepository.save(UserRole(
        user = encodedUser,
        role = UserRole.Role.GUEST
    ))
  }


}

*/