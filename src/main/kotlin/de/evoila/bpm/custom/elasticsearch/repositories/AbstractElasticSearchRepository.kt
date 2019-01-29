package de.evoila.bpm.custom.elasticsearch.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.BaseEntity
import kotlinx.serialization.json.Json
import org.elasticsearch.action.DocWriteResponse
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.common.xcontent.XContentType
import org.slf4j.LoggerFactory
import org.springframework.data.repository.CrudRepository

abstract class AbstractElasticSearchRepository<T : BaseEntity>(
    val elasticSearchRestTemplate: ElasticSearchRestTemplate
) : CrudRepository<T, String> {

  abstract val index: String
  val type: String = "_doc"


  abstract fun serializeObject(entity: T): String

  override fun <S : T> save(entity: S): S {

    val indexRequest = IndexRequest()
        .type(type)
        .index(index)
        .id(entity.id)

    val body = serializeObject(entity)

    indexRequest.source(body, XContentType.JSON)

    val indexResponse = elasticSearchRestTemplate.performIndexRequest(indexRequest)

    if (indexResponse.result !in listOf(DocWriteResponse.Result.CREATED, DocWriteResponse.Result.UPDATED))
      log.error("$entity has not been saved/updated in ElasticSearch")

    return entity
  }

  override fun <S : T> saveAll(entities: Iterable<S>): List<S> {
    return entities.map {
      save(it)
      return@map it
    }
  }

  override fun findAllById(ids: MutableIterable<String>): List<T> {
    return ids.map { id ->
      return@map findById(id).get()
    }.toMutableList()
  }

  override fun existsById(id: String): Boolean {
    val response = requestById(id)

    return response.isExists
  }

  override fun deleteById(id: String) {
    val deleteRequest = DeleteRequest(index, type, id)
    elasticSearchRestTemplate.performDeleteRequest(deleteRequest)
  }

  override fun delete(entity: T) {
    deleteById(entity.id)
  }

  override fun deleteAll() {
    log.info("Not implemented as it is not meant to be ever used.")
  }

  override fun deleteAll(entities: Iterable<T>) {
    entities.forEach {
      deleteById(it.id)
    }
  }

  fun requestById(id: String): GetResponse {
    val request = GetRequest().index(index).type(type).id(id)

    return elasticSearchRestTemplate.performGetRequest(request)
  }

  override fun count(): Long {
    val searchRequest = SearchRequest().indices(index).types(type)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.totalHits
  }

  companion object {
    private val log = LoggerFactory.getLogger(AbstractElasticSearchRepository::class.java)
  }
}