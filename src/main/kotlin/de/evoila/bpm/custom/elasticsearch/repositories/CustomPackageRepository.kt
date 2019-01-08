package de.evoila.bpm.custom.elasticsearch.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.Package
import org.elasticsearch.action.DocWriteResponse.Result.CREATED
import org.elasticsearch.action.DocWriteResponse.Result.UPDATED
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CustomPackageRepository(
    private val elasticSearchRestTemplate: ElasticSearchRestTemplate
) : PagingAndSortingRepository<Package, String> {

  override fun <S : Package> save(entity: S): S {

    val objectMapper = ObjectMapper()
    val indexRequest = IndexRequest(
        INDEX, TYPE, entity.id
    )
    indexRequest.source(objectMapper.writeValueAsString(entity))

    val indexResponse = elasticSearchRestTemplate.performIndexRequest(indexRequest)

    if (indexResponse.result !in listOf(CREATED, UPDATED))
      log.error("$entity has not been saved/updated in ElasticSearch")

    return entity
  }

  fun findByVendorAndNameAndVersion(vendor: String, name: String, version: String): Optional<Package> {
    TODO()
  }


  override fun findAll(sort: Sort): List<Package> {
    val searchRequest = SearchRequest(INDEX, TYPE)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)
    val objectMapper = ObjectMapper()

    return response.hits.map {
      objectMapper.readValue(it.sourceAsString, Package::class.java)
    }.toMutableList()
  }

  override fun findAll(pageable: Pageable): Page<Package> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun findAll(): List<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(QueryBuilders.matchAllQuery())
    val searchRequest = SearchRequest(INDEX, TYPE)
    searchRequest.source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)
    val objectMapper = ObjectMapper()

    return response.hits.map {
      objectMapper.readValue(it.sourceAsString, Package::class.java)
    }
  }

  override fun deleteById(id: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun deleteAll() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }


  override fun <S : Package?> saveAll(entities: Iterable<S>): List<S> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun count(): Long {
    val searchRequest = SearchRequest(INDEX, TYPE)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.totalHits
  }

  override fun findAllById(ids: MutableIterable<String>): List<Package> {
    return ids.map { id ->
      return@map findById(id).get()
    }.toMutableList()
  }

  override fun existsById(id: String): Boolean {
    val response = requestById(id)

    return response.isExists
  }

  override fun delete(entity: Package) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun findById(id: String): Optional<Package> {
    val response = requestById(id)

    return if (response.isExists) {
      val mapper = ObjectMapper()
      val result = mapper.readValue(response.sourceAsString, Package::class.java)

      Optional.of(result)
    } else {
      Optional.empty()
    }
  }

  private fun requestById(id: String): GetResponse {
    val request = GetRequest(INDEX, TYPE, id)

    return elasticSearchRestTemplate.performGetRequest(request)
  }

  override fun deleteAll(entities: Iterable<Package>) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun getPackagesByName(name: String): List<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(MatchQueryBuilder("name", name))
    val searchRequest = SearchRequest(INDEX, TYPE)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)
    val objectMapper = ObjectMapper()

    return response.hits.map {
      objectMapper.readValue(it.sourceAsString, Package::class.java)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(CustomPackageRepository::class.java)
    private const val INDEX = "packages"
    private const val TYPE = "doc"
  }
}
