package de.evoila.bpm.repositories

import de.evoila.bpm.entities.Package
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.*


interface PackageRepository : ElasticsearchRepository<Package, String> {

  fun findByName(name: String): List<Package>

  fun findByVendor(name: String): List<Package>

  fun findByVendorAndNameAndVersion(vendor : String, name: String, version: String): List<Package>
}