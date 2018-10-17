package de.evoila.bpm.repositories

import de.evoila.bpm.entities.Blob
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface BlobRepository : ElasticsearchRepository<Blob, String> {

  fun findByNameAndVersion(name: String, version: String): Blob?
}