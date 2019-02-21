package de.evoila.bpm.custom.elasticsearch.repositories

import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.User
import kotlinx.serialization.json.Json
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
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

  override fun findById(id: String): User? {
    val response = requestById(id)

    return if (response.isExists) {
      Json.parse(User.serializer(), response.sourceAsString)
    } else {
      null
    }
  }

  fun findByEmail(email: String): User? {
    val searchSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder("email.keyword", email))
    searchSourceBuilder.query(boolQueryBuilder)

    val searchRequest = SearchRequest().indices(index).types(type).source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map { Json.parse(User.serializer(), it.sourceAsString) }.firstOrNull()
  }
}