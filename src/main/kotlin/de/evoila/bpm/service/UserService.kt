package de.evoila.bpm.service

import de.evoila.bpm.custom.elasticsearch.repositories.CustomUserRepository
import de.evoila.bpm.entities.User
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: CustomUserRepository
) {


  fun saveUser(id: String, email: String) {
    val user = User(email = email)
    user.id = id

    userRepository.save(user)

  }

}