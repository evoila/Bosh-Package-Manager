package de.evoila.bpm.custom.elasticsearch.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.User
import kotlinx.serialization.json.Json
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CustomUserRepository(
    elasticSearchRestTemplate: ElasticSearchRestTemplate
) : AbstractElasticSearchRepository<User>(elasticSearchRestTemplate) {


  override fun serializeObject(entity: User): String {
    return Json.stringify(User.serializer(), entity)
  }

  override val index: String = "users"

  override fun findAll(): List<User> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(QueryBuilders.matchAllQuery())
    val searchRequest = SearchRequest(index, type)
    searchRequest.source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map {
      Json.parse(User.serializer(), it.sourceAsString)
    }
  }

  override fun findById(id: String): Optional<User> {
    val response = requestById(id)

    return if (response.isExists) {
      val result = Json.parse(User.serializer(), response.sourceAsString)

      Optional.of(result)
    } else {
      Optional.empty()
    }
  }

  fun findByEmail(email: String): User? {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(MatchQueryBuilder("email", email))

    val searchRequest = SearchRequest().indices(index).types(type).source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map { Json.parse(User.serializer(), it.sourceAsString) }.firstOrNull()
  }
}