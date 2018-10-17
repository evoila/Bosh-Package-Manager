package de.evoila.bpm.service

import de.evoila.bpm.exceptions.InvalidDependencyException
import de.evoila.bpm.exceptions.PackageStoringException
import de.evoila.bpm.repositories.PackageRepository
import de.evoila.bpm.rest.bodies.PackageBody
import de.evoila.bpm.entities.Package
import de.evoila.bpm.repositories.BlobRepository
import de.evoila.bpm.rest.bodies.BlobBody
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.Exception
import java.lang.IllegalArgumentException

@Service
class PackageService(
    val packageRepository: PackageRepository,
    val blobRepository: BlobRepository
) {

  @Throws(PackageStoringException::class)
  fun saveRelease(packageBody: PackageBody
  ): Package {

    return packageRepository.findByNameAndVersion(packageBody.name, packageBody.version).orElseGet {

      try {
        validateDependencies(packageBody.dependencies)

        val newPackage = Package(
            name = packageBody.name,
            version = packageBody.version,
            spec = packageBody.spec.orEmpty(),
            blobs = validateBlobsAndReturnIds(packageBody.blobs),
            dependencies = validateDependencies(packageBody.dependencies),
            packaging = packageBody.packaging.orEmpty()
        )

        return@orElseGet packageRepository.save(newPackage)

      } catch (e: IllegalArgumentException) {
        log.error(e.message)

        throw PackageStoringException(e.message)
      }
    }
  }

  fun validateBlobsAndReturnIds(blobs: List<BlobBody>): List<String> {

    return blobs.map { blobBody ->

      blobRepository.findByNameAndVersion(blobBody.name, blobBody.version)?.let { return@map it.id }
          ?: throw Exception("Blob ${blobBody.name}:${blobBody.version}  not found")
    }
  }

  @Throws(InvalidDependencyException::class)
  fun validateDependencies(dependencies: List<PackageBody.Dependency>): List<String> =
      dependencies.map {
        packageRepository.findByNameAndVersion(it.name, it.version)
            .orElseThrow { IllegalArgumentException("Package ${it.name}:${it.version} not found") }.id
      }

  fun getPackageWithDependenciesAsList(uuid: String): List<Package> {

    val rootPackage = packageRepository.findById(uuid).get()
    val packages = rootPackage.dependencies.map { packageRepository.findById(it).get() }

    return packages.plus(rootPackage)
  }

  companion object {
    private val log = LoggerFactory.getLogger(PackageService::class.java)
  }
}