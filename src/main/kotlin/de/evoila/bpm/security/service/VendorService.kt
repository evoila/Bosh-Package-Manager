package de.evoila.bpm.security.service

import de.evoila.bpm.security.exceptions.UserExistsException
import de.evoila.bpm.security.exceptions.VendorExistsException
import de.evoila.bpm.security.model.User
import de.evoila.bpm.security.model.UserRole
import de.evoila.bpm.security.model.Vendor
import de.evoila.bpm.security.repositories.UserRepository
import de.evoila.bpm.security.repositories.VendorRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VendorService(
    val vendorRepository: VendorRepository,
    val userRepository: UserRepository
) {

  fun addVendorIfNew(vendor: Vendor, user: User) {

    if (vendorRepository.existsByName(vendor.name)) {
      throw VendorExistsException("Name '${vendor.name} already taken")
    }

    vendor.apply {
      admins = setOf(user)
    }

    user.apply {
      this.roles.add(UserRole(
          role = UserRole.Role.VENDOR,
          user = this
      ))
    }

    userRepository.save(user)
    vendorRepository.save(vendor)
  }

  fun hasAccess(user: User, vendor: Vendor): Boolean = vendor.admins.stream().anyMatch {
    it.signingKey == user.signingKey
  }

  fun addMemberToVendor(vendorName: String, member: String) {
    val admin: User = SecurityContextHolder.getContext().authentication.principal as User

    val user = userRepository.findByUsername(member)
        ?: throw UserExistsException("User $member does not exist.")

    val vendor = vendorRepository.findByName(vendorName)
        ?: throw VendorExistsException("Vendor $vendorName does not exist.")

    if (!hasAccess(admin, vendor)) {
      log.info("Unauthorized")
      return
    }

    user.apply {
      this.roles.add(UserRole(
          role = UserRole.Role.VENDOR,
          user = this
      ))
    }

    vendor.apply {
      members = members.plus(user)
    }

    userRepository.save(admin)
    vendorRepository.save(vendor)

  }

  companion object {

    val log: Logger = LoggerFactory.getLogger(VendorService::class.java)
  }
}