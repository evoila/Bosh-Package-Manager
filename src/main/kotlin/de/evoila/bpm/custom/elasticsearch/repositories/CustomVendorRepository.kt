package de.evoila.bpm.custom.elasticsearch.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.Vendor
import kotlinx.serialization.json.Json
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CustomVendorRepository(
    elasticSearchRestTemplate: ElasticSearchRestTemplate
) : AbstractElasticSearchRepository<Vendor>(elasticSearchRestTemplate) {

  override fun serializeObject(entity: Vendor): String {
    return Json.stringify(Vendor.serializer(), entity)
  }

  override val index: String = "vendors"

  override fun findAll(): List<Vendor> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(QueryBuilders.matchAllQuery())
    val searchRequest = SearchRequest(index, type)
    searchRequest.source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map {
      Json.parse(Vendor.serializer(), it.sourceAsString)
    }
  }

  override fun findById(id: String): Optional<Vendor> {
    val response = requestById(id)

    return if (response.isExists) {
      val result = Json.parse(Vendor.serializer(), response.sourceAsString)

      Optional.of(result)
    } else {
      Optional.empty()
    }
  }

  fun findByName(name: String): Vendor? {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(MatchQueryBuilder("name", name))


    val searchRequest = SearchRequest().indices(index).types(type).source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map { Json.parse(Vendor.serializer(), it.sourceAsString) }.firstOrNull()
  }

  companion object {
    private val log = LoggerFactory.getLogger(CustomPackageRepository::class.java)
  }
}