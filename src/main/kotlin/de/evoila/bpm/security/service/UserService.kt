package de.evoila.bpm.security.service

import de.evoila.bpm.security.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository
) : UserDetailsService {


  override fun loadUserByUsername(username: String): UserDetails {

    return userRepository.findByUsername(username)
        ?: throw UsernameNotFoundException("Did not find user $username")
  }


}