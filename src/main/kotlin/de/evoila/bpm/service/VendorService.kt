package de.evoila.bpm.service

import de.evoila.bpm.custom.elasticsearch.repositories.CustomUserRepository
import de.evoila.bpm.custom.elasticsearch.repositories.CustomVendorRepository
import de.evoila.bpm.entities.Vendor
import de.evoila.bpm.exceptions.UnauthorizedException
import de.evoila.bpm.exceptions.UserNotFoundException
import de.evoila.bpm.exceptions.VendorNotFoundException
import org.springframework.stereotype.Service

@Service
class VendorService(
    val customVendorRepository: CustomVendorRepository,
    val userRepository: CustomUserRepository
) {

  fun isMemberOf(username: String, vendorName: String): Boolean = customVendorRepository.findByName(vendorName)?.let {
    return it.isMember(username)
  } ?: false

  fun createNewVendor(name: String, creator: String) {
    customVendorRepository.findByName(name)?.let { throw IllegalArgumentException("$name already exists") }
    customVendorRepository.save(Vendor(
        name = name,
        members = setOf(creator)
    ))
  }

  fun addMemberToVendor(admin: String, vendorName: String, email: String) {
    val user = userRepository.findByEmail(email)
        ?: throw UserNotFoundException("No user could be found for this email.")
    val vendor = customVendorRepository.findByName(vendorName)
        ?: throw VendorNotFoundException("The vendor could not be found")

    if (vendor.isMember(admin)) {
      val updatedVendor = vendor.copy(members = vendor.members.plus(user.id))

      customVendorRepository.save(updatedVendor)
    } else {
      throw UnauthorizedException("User is not a member of $vendorName")
    }
  }

  fun vendorsForUsers(name: String): List<Vendor> {
    return customVendorRepository.memberOfByName(name)
  }

  fun findByName(name: String): Vendor? {
    return customVendorRepository.findByName(name)
  }
}