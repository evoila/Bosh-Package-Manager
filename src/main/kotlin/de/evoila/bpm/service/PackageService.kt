package de.evoila.bpm.service

import de.evoila.bpm.entities.Package
import de.evoila.bpm.entities.Package.AccessLevel.*
import de.evoila.bpm.exceptions.PackageNotFoundException
import de.evoila.bpm.helpers.PendingPackages
import de.evoila.bpm.repositories.PackageRepository
import de.evoila.bpm.rest.bodies.PackageBody
import de.evoila.bpm.security.model.User
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class PackageService(
    val packageRepository: PackageRepository
) {

  fun getPackages(name: String): List<Package> {

    return packageRepository.findByName(name)
  }

  @Throws(PackageNotFoundException::class)
  fun accessPackage(vendor: String, name: String, version: String, user: User?): Package {


    val unfiltered = packageRepository.findByVendorAndNameAndVersion(vendor, name, version)

    val packages = user?.let {
      unfiltered.filter { boshPackage ->

        val vendors = it.adminOf.plus(it.memberOf)

        boshPackage.accessLevel == PUBLIC ||
            boshPackage.accessLevel == VENDOR && vendors.stream().anyMatch { vendor -> vendor.name == boshPackage.vendor } ||
            boshPackage.accessLevel == PRIVATE && boshPackage.signedWith == it.signingKey
      }
    } ?: unfiltered.filter { it.accessLevel == PUBLIC }

    return packages.find {
      it.name == name && it.vendor == vendor && it.version == version
    } ?: throw PackageNotFoundException("didn't not find a package with vendor : $vendor , name : $name:$version")
  }

  fun checkIfPresent(packageBody: PackageBody): Package? {

    return packageRepository.findByName(packageBody.name).find {
      it.name == packageBody.name && it.vendor == packageBody.vendor && it.version == packageBody.version
    }
  }

  fun putPendingPackage(packageBody: PackageBody, signingKey: String
  ): String {

    log.info("Pending package: $packageBody")

    packageRepository.findByVendorAndNameAndVersion(
        vendor = packageBody.vendor,
        name = packageBody.name,
        version = packageBody.version).find {
      it.name == packageBody.name && it.vendor == packageBody.vendor && it.version == packageBody.version
    }?.let {
      packageRepository.deleteById(it.id)
      //TODO delete outdated file in the S3 bucket!!!
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

  fun alterAccessLevel(vendor: String, name: String, version: String, user: User, accessLevel: Package.AccessLevel): Int {

    val pack = accessPackage(vendor, name, version, user)
    packageRepository.save(pack.changeAccessLevel(accessLevel))

    return if (accessLevel == PUBLIC) {
      //TODO make control mechanism to notify admins for a package review before it goes public
      202
    } else {
      200
    }
  }

  fun savePendingPackage(key: String) {

    val packageToSave = pendingPackages.remove(key)
        ?: throw PackageNotFoundException("The Package does not exist.")

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