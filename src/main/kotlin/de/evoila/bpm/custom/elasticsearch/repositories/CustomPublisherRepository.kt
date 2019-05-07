package de.evoila.bpm.custom.elasticsearch.repositories

import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.Publisher
import kotlinx.serialization.json.Json
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class CustomPublisherRepository(
    elasticSearchRestTemplate: ElasticSearchRestTemplate
) : AbstractElasticSearchRepository<Publisher>(elasticSearchRestTemplate) {

  override fun serializeObject(entity: Publisher): String {
    return Json.stringify(Publisher.serializer(), entity)
  }

  override val index: String = "publishers"

  override fun findById(id: String): Publisher? {
    val response = requestById(id)

    return if (response.isExists) {
      Json.parse(Publisher.serializer(), response.sourceAsString)
    } else {
      null
    }
  }

  fun findByName(name: String): Publisher? {
    val searchSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_NAME + KEYWORD, name))
    searchSourceBuilder.query(boolQueryBuilder)
    val searchRequest = SearchRequest().indices(index).types(type).source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    if (response.hits.hits.size > 1) {
      log.warn("Multiple Publisher hits where one is expected!!")
    }

    return response.hits.map { Json.parse(Publisher.serializer(), it.sourceAsString) }.firstOrNull()
  }

  fun memberOfByName(username: String): List<Publisher> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(MatchQueryBuilder(FIELD_MEMBERS + KEYWORD, username))
    val searchRequest = SearchRequest().indices(index).types(type).source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map { Json.parse(Publisher.serializer(), it.sourceAsString) }
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(CustomPublisherRepository::class.java)
    private const val FIELD_NAME = "name"
    private const val FIELD_MEMBERS = "members"
  }
}