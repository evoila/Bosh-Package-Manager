package de.evoila.bpm.security.filter

import de.evoila.bpm.custom.elasticsearch.repositories.CustomVendorRepository
import org.springframework.stereotype.Service
import de.evoila.bpm.entities.Package
import de.evoila.bpm.entities.Package.AccessLevel.*

@Service
class PackageAccessFilter(
    var vendorRepository: CustomVendorRepository
) {

  fun filterPackageList(username: String?, packages: List<Package>): List<Package> = packages.filter { pack ->
    checkAccessToSinglePackage(username, pack)
  }

  fun checkAccessToSinglePackage(username: String?, pack: Package): Boolean = when (pack.accessLevel) {
    PUBLIC -> true
    PRIVATE -> username?.let { hasPrivateAccess(username, pack) } ?: false
    VENDOR -> username?.let { hasVendorAccess(username, pack) } ?: false
  }

  private fun hasVendorAccess(username: String, pack: Package): Boolean = vendorRepository.findByName(pack.vendor)?.isMember(username)
      ?: false

  private fun hasPrivateAccess(username: String, pack: Package): Boolean = username == pack.signedWith
}