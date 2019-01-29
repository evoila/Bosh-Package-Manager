package de.evoila.bpm.service

import de.evoila.bpm.custom.elasticsearch.repositories.CustomPackageRepository
import de.evoila.bpm.entities.Package
import de.evoila.bpm.entities.Package.AccessLevel.*
import de.evoila.bpm.exceptions.PackageNotFoundException
import de.evoila.bpm.helpers.PendingPackages
import de.evoila.bpm.rest.bodies.PackageBody
import de.evoila.bpm.security.filter.PackageAccessFilter
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class PackageService(
    val customPackageRepository: CustomPackageRepository,
    val packageAccessFilter: PackageAccessFilter
) {

  fun getAllPackages(username: String?, pageable: Pageable): Page<Package> =
      packageAccessFilter.filterPackageList(username, customPackageRepository.findAll(pageable))

  fun getPackagesByName(username: String?, packageName: String): List<Package> =
      packageAccessFilter.filterPackageList(username, customPackageRepository.getPackagesByName(packageName))

  fun findById(id: String): Package? = customPackageRepository.findById(id).orElseGet { null }

  @Throws(PackageNotFoundException::class)
  fun accessPackage(vendor: String, name: String, version: String, username: String?): Package {

    val pack = customPackageRepository.findByVendorAndNameAndVersion(vendor, name, version)
        ?: throw PackageNotFoundException("didn't not find a package with vendor : $vendor , name : $name:$version")

    if (!packageAccessFilter.checkAccessToSinglePackage(username, pack)) {
      throw  IllegalAccessError("User has no access to this package")
    }

    return pack
  }

  fun checkIfPresent(packageBody: PackageBody): Package? {
    return customPackageRepository.findByVendorAndNameAndVersion(packageBody.vendor, packageBody.name, packageBody.version)
  }

  fun putPendingPackage(packageBody: PackageBody, signingKey: String
  ): String {
    log.info("Pending package: $packageBody")

    customPackageRepository.findByVendorAndNameAndVersion(
        vendor = packageBody.vendor,
        name = packageBody.name,
        version = packageBody.version)?.let {
      customPackageRepository.deleteById(it.id)
      //   TODO delete outdated file in the S3 bucket !!
    }

    val s3location = "${UUID.randomUUID()}.bpm"

    pendingPackages.put(s3location, Package(
        name = packageBody.name,
        vendor = packageBody.vendor,
        version = packageBody.version,
        uploadDate = Instant.now().toString(),
        s3location = s3location,
        files = packageBody.files,
        dependencies = packageBody.dependencies,
        stemcell = packageBody.stemcell,
        accessLevel = PRIVATE,
        signedWith = signingKey,
        description = packageBody.description
    ))

    return s3location
  }

  fun alterAccessLevel(id: String, username: String, accessLevel: Package.AccessLevel) {
    val pack = customPackageRepository.findById(id)
        .orElseThrow { PackageNotFoundException("Did not find a package for the given id") }
    alterAccessLevel(username, accessLevel, pack)
  }

  fun alterAccessLevel(username: String, accessLevel: Package.AccessLevel, pack: Package) {
    customPackageRepository.save(pack.changeAccessLevel(accessLevel))

    pack.dependencies?.forEach {
      val dependency = accessPackage(it.vendor, it.name, it.version, username)

      if (dependency.accessLevel.isAbove(accessLevel)) {
        alterAccessLevel(username, accessLevel, dependency)
      }
    }
  }

  fun savePendingPackage(key: String) {
    val packageToSave = pendingPackages.remove(key)
        ?: throw PackageNotFoundException("The Package does not exist.")

    customPackageRepository.save(packageToSave)
    log.info("Save package: $packageToSave")
  }

  @Async
  @Scheduled(fixedRate = 1800000)
  fun startCleanUp() {
    pendingPackages.cleanUp()
  }

  companion object {
    private val log = LoggerFactory.getLogger(PackageService::class.java)
    private val pendingPackages = PendingPackages(900)
  }
}