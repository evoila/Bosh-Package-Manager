package de.evoila.bpm.custom.elasticsearch.repositories

import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.Vendor
import kotlinx.serialization.json.Json
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
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
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder("name", name))
    searchSourceBuilder.query(boolQueryBuilder)

    val searchRequest = SearchRequest().indices(index).types(type).source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map { Json.parse(Vendor.serializer(), it.sourceAsString) }.firstOrNull()
  }

  fun memberOfByName(username: String): List<Vendor> {

    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(MatchQueryBuilder("members", username))

    val searchRequest = SearchRequest().indices(index).types(type).source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map { Json.parse(Vendor.serializer(), it.sourceAsString) }
  }
}