package de.evoila.bpm.repositories

import de.evoila.bpm.entities.Package
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.rest.core.annotation.RestResource
import org.springframework.web.bind.annotation.CrossOrigin

@CrossOrigin(origins = ["http://localhost:4200"], maxAge = 3600)
interface PackageRepository : ElasticsearchRepository<Package, String> {

  @RestResource(exported = false)
  override fun <S : Package?> save(entity: S): S

  @RestResource(exported = false)
  override fun delete(entity: Package)

  fun findByName(name: String): List<Package>

  @CrossOrigin(origins = ["http://localhost:4200"], maxAge = 3600)
  fun findByVendorAndNameAndVersion(vendor: String, name: String, version: String): List<Package>
}