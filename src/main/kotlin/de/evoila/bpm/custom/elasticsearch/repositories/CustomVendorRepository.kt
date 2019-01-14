package de.evoila.bpm.custom.elasticsearch.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.Vendor
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

  override val index: String = "vendors"

  override fun findAll(): List<Vendor> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(QueryBuilders.matchAllQuery())
    val searchRequest = SearchRequest(index, type)
    searchRequest.source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)
    val objectMapper = ObjectMapper()

    return response.hits.map {
      objectMapper.readValue(it.sourceAsString, Vendor::class.java)
    }
  }

  override fun findById(id: String): Optional<Vendor> {
    val response = requestById(id)

    return if (response.isExists) {
      val mapper = ObjectMapper()
      val result = mapper.readValue(response.sourceAsString, Vendor::class.java)

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
    val objectMapper = ObjectMapper()

    return response.hits.map { objectMapper.readValue(it.sourceAsString, Vendor::class.java) }.firstOrNull()
  }

  companion object {
    private val log = LoggerFactory.getLogger(CustomPackageRepository::class.java)
  }
}