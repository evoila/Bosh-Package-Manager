package de.evoila.bpm.service

import de.evoila.bpm.custom.elasticsearch.repositories.CustomPackageRepository
import de.evoila.bpm.entities.Package
import de.evoila.bpm.entities.Package.AccessLevel.*
import de.evoila.bpm.exceptions.PackageNotFoundException
import de.evoila.bpm.helpers.PendingPackages
import de.evoila.bpm.rest.bodies.PackageBody
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
   private val packageRepository: CustomPackageRepository,
   private val amazonS3Service: AmazonS3Service
) {

  fun getAllPackages(username: String?, pageable: Pageable): Page<Package> =
      packageRepository.findAll(pageable, username)

  fun getPackagesByVendor(username: String?, pageable: Pageable, vendor: String): Page<Package> =
      packageRepository.searchByVendor(pageable, username, vendor)

  fun getPackagesByName(username: String?, packageName: String): List<Package> =
      packageRepository.searchPackagesByName(packageName, username)

  fun findById(id: String): Package? = packageRepository.findById(id)

  @Throws(PackageNotFoundException::class)
  fun accessPackage(vendor: String, name: String, version: String, username: String?): Package {

    return packageRepository.findByVendorAndNameAndVersion(vendor, name, version, username)
        ?: throw PackageNotFoundException("didn't not find a package with vendor : $vendor , name : $name:$version")
  }

  fun checkIfPresent(packageBody: PackageBody): Package? {
    return packageRepository.findByVendorAndNameAndVersion(packageBody.vendor, packageBody.name, packageBody.version)
  }

  fun putPendingPackage(packageBody: PackageBody, signingKey: String
  ): String {
    log.info("Pending package: $packageBody")

    packageRepository.findByVendorAndNameAndVersion(
        vendor = packageBody.vendor,
        name = packageBody.name,
        version = packageBody.version)?.let {

      packageRepository.deleteById(it.id)
      amazonS3Service.deleteObject(it.s3location)
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
        description = packageBody.description,
        size = 0,
        url = packageBody.url
    ))

    return s3location
  }

  fun alterAccessLevel(id: String, username: String, accessLevel: Package.AccessLevel) {
    val pack = packageRepository.findById(id)
        ?: throw PackageNotFoundException("Did not find a package for the given id")
    alterAccessLevel(username, accessLevel, pack)
  }

  fun alterAccessLevel(username: String, accessLevel: Package.AccessLevel, pack: Package) {
    packageRepository.save(pack.changeAccessLevel(accessLevel))

    pack.dependencies?.forEach {
      val dependency = accessPackage(it.vendor, it.name, it.version, username)

      if (dependency.accessLevel.isAbove(accessLevel)) {
        alterAccessLevel(username, accessLevel, dependency)
      }
    }
  }

  fun deletePackageIfAllowed(vendor: String, name: String, version: String) {
    val pack = packageRepository.findByVendorAndNameAndVersion(vendor, name, version)
    pack?.let {
      packageRepository.deleteById(it.id)
      amazonS3Service.deleteObject(it.s3location)
    } ?: throw PackageNotFoundException("the package does not exist.")
  }

  fun savePendingPackage(key: String, size: Long) {
    val packageToSave = pendingPackages.remove(key)
        ?: throw PackageNotFoundException("The Package does not exist.")
    packageToSave.size = size

    packageRepository.save(packageToSave)
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