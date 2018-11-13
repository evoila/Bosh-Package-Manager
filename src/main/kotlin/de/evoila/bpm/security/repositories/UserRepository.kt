package de.evoila.bpm.security.repositories

import de.evoila.bpm.security.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface UserRepository : JpaRepository<User, Int> {

  fun findByUsername(username: String): User?

  fun findByEmail(email: String): User?


}