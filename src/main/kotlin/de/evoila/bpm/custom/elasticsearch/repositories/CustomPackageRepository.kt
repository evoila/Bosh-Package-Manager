package de.evoila.bpm.custom.elasticsearch.repositories

import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.Package
import kotlinx.serialization.json.Json
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.*
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CustomPackageRepository(
    elasticSearchRestTemplate: ElasticSearchRestTemplate
) : AbstractElasticSearchRepository<Package>(
    elasticSearchRestTemplate
) {

  override val index: String = "packages"

  override fun serializeObject(entity: Package): String {

    return Json.stringify(Package.serializer(), entity)
  }

  fun findByVendorAndNameAndVersion(vendor: String, name: String, version: String): Package? {

    val searchSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.filter(MatchQueryBuilder("vendor_keyword", vendor).operator(Operator.AND))
    boolQueryBuilder.filter(MatchQueryBuilder("name_keyword", name).operator(Operator.AND))
    boolQueryBuilder.filter(MatchQueryBuilder("version_keyword", version).operator(Operator.AND))

    searchSourceBuilder
        .query(boolQueryBuilder)
        .size(1)

    val searchRequest = SearchRequest().indices(index).types(type)
        .source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    if (response.hits.totalHits > 1) {
      log.info("Multiple Hits!!!!")
      response.hits.forEach {
        log.info(it.toString())
      }
    }

    return response.hits.map { Json.parse(Package.serializer(), it.sourceAsString) }.firstOrNull()
  }

  fun findAll(sort: Sort): List<Package> {
    val searchRequest = SearchRequest().indices(index).types(type)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map {
      Json.parse(Package.serializer(), it.sourceAsString)
    }.toMutableList()
  }

  fun findAll(pageable: Pageable): Page<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
        .from(pageable.pageNumber)
        .size(pageable.pageSize)

    pageable.sort.forEach {
      searchSourceBuilder.sort(FieldSortBuilder(it.property)
          .order(SortOrder.fromString(it.direction.name)))
    }

    val searchRequest = SearchRequest().indices(index).types(type).source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    val content = response.hits.map {
      Json.parse(Package.serializer(), it.sourceAsString)
    }

    return PageImpl(content, pageable, response.hits.totalHits)
  }

  override fun findAll(): List<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(QueryBuilders.matchAllQuery())
    val searchRequest = SearchRequest().indices(index).types(type)
    searchRequest.source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map {
      Json.parse(Package.serializer(), it.sourceAsString)
    }
  }

  override fun findById(id: String): Optional<Package> {
    val response = requestById(id)

    return if (response.isExists) {
      val result = Json.parse(Package.serializer(), response.sourceAsString)

      Optional.of(result)
    } else {
      Optional.empty()
    }
  }

  fun getPackagesByName(name: String): List<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(MatchQueryBuilder("name", name))
    val searchRequest = SearchRequest().indices(index).types(type).source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    return response.hits.map {
      Json.parse(Package.serializer(), it.sourceAsString)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(CustomPackageRepository::class.java)
  }
}