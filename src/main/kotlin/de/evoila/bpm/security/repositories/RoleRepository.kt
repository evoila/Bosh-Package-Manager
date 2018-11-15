package de.evoila.bpm.security.repositories

import de.evoila.bpm.security.model.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(exported = false)
interface RoleRepository : JpaRepository<UserRole, Long>