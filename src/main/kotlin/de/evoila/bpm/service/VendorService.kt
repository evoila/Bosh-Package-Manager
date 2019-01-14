package de.evoila.bpm.service

import de.evoila.bpm.custom.elasticsearch.repositories.CustomVendorRepository
import de.evoila.bpm.entities.Vendor
import org.springframework.stereotype.Service

@Service
class VendorService(
    val customVendorRepository: CustomVendorRepository
) {

  fun isMemberOf(username: String, name: String): Boolean = customVendorRepository.findByName(name)?.let {
    return it.isMember(username)
  } ?: false

  fun createNewVendor(name: String, creator: String) {

    customVendorRepository.findByName(name)?.let { throw IllegalArgumentException("$name already exists") }
    customVendorRepository.save(Vendor(
        name = name,
        members = listOf(creator)
    ))
  }
}