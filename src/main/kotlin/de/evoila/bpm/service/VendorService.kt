package de.evoila.bpm.service

import de.evoila.bpm.custom.elasticsearch.repositories.CustomVendorRepository
import org.springframework.stereotype.Service

@Service
class VendorService(
    val customVendorRepository: CustomVendorRepository
) {

  fun isMemberOf(username: String, name: String): Boolean = customVendorRepository.findByName(name)?.let {
    return it.isMember(name)
  } ?: false
}