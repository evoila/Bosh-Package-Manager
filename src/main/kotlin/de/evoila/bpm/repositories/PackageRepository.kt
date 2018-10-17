package de.evoila.bpm.repositories

import de.evoila.bpm.entities.Package
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.*


interface PackageRepository : ElasticsearchRepository<Package, String> {

  fun findByNameAndVersion(name: String, version: String): Optional<Package>
}