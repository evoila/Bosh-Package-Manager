package de.evoila.bpm.custom.elasticsearch.repositories

import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate

import de.evoila.bpm.entities.BaseEntity
import org.elasticsearch.action.DocWriteResponse
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.common.xcontent.XContentType
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.util.*

@Repository
abstract class AbstractElasticSearchRepository<T : BaseEntity>(
    val elasticSearchRestTemplate: ElasticSearchRestTemplate
) {

  abstract val index: String


  abstract fun serializeObject(entity: T): String

  abstract fun findById(id: String): T?

  fun save(entity: T): T {

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

  fun saveAll(entities: Iterable<T>): List<T> {
    return entities.map {
      save(it)
      return@map it
    }
  }


  fun findAllById(ids: MutableIterable<String>): List<T> {
    return ids.map { id ->
      findById(id)
    }.requireNoNulls()
  }

  fun existsById(id: String): Boolean {
    val response = requestById(id)

    return response.isExists
  }

  fun deleteById(id: String) {
    val deleteRequest = DeleteRequest(index, type, id)
    elasticSearchRestTemplate.performDeleteRequest(deleteRequest)
  }

  fun delete(entity: T) {
    deleteById(entity.id)
  }

  fun deleteAll() {
    log.info("Not implemented as it is not meant to be ever used.")
  }

  fun deleteAll(entities: Iterable<T>) {
    entities.forEach {
      deleteById(it.id)
    }
  }

  fun requestById(id: String): GetResponse {
    val request = GetRequest().index(index).type(type).id(id)

    return elasticSearchRestTemplate.performGetRequest(request)
  }

  fun count(): Long {
    val searchRequest = SearchRequest().indices(index).types(type)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.totalHits
  }

  companion object {
    private val log = LoggerFactory.getLogger(AbstractElasticSearchRepository::class.java)
    const val type: String = "_doc"
    const val KEYWORD = ".keyword"
  }
}