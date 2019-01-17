package de.evoila.bpm.custom.elasticsearch.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.User
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

  override val index: String = "users"

  override fun findAll(): List<User> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(QueryBuilders.matchAllQuery())
    val searchRequest = SearchRequest(index, type)
    searchRequest.source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)
    val objectMapper = ObjectMapper()

    return response.hits.map {
      objectMapper.readValue(it.sourceAsString, User::class.java)
    }
  }

  override fun findById(id: String): Optional<User> {
    val response = requestById(id)

    return if (response.isExists) {
      val mapper = ObjectMapper()
      val result = mapper.readValue(response.sourceAsString, User::class.java)

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
    val objectMapper = ObjectMapper()

    return response.hits.map { objectMapper.readValue(it.sourceAsString, User::class.java) }.firstOrNull()
  }
}