package de.evoila.bpm.custom.elasticsearch.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.Package
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
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

  fun findByVendorAndNameAndVersion(vendor: String, name: String, version: String): Package? {

    val searchSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.should(MatchQueryBuilder("vendor", vendor))
    boolQueryBuilder.should(MatchQueryBuilder("name", name))
    boolQueryBuilder.should(MatchQueryBuilder("version", version))

    searchSourceBuilder
        .query(boolQueryBuilder)
        .size(1)

    val searchRequest = SearchRequest().indices(index).types(type)
        .source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)
    val objectMapper = ObjectMapper()

    return response.hits.map { objectMapper.readValue(it.sourceAsString, Package::class.java) }.firstOrNull()
  }

  fun findAll(sort: Sort): List<Package> {
    val searchRequest = SearchRequest().indices(index).types(type)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)
    val objectMapper = ObjectMapper()

    return response.hits.map {
      objectMapper.readValue(it.sourceAsString, Package::class.java)
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

    val objectMapper = ObjectMapper()
    val content = response.hits.map {
      objectMapper.readValue(it.sourceAsString, Package::class.java)
    }

    return PageImpl(content, pageable, response.hits.totalHits)
  }

  override fun findAll(): List<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(QueryBuilders.matchAllQuery())
    val searchRequest = SearchRequest().indices(index).types(type)
    searchRequest.source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)
    val objectMapper = ObjectMapper()

    return response.hits.map {
      objectMapper.readValue(it.sourceAsString, Package::class.java)
    }
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

  fun getPackagesByName(name: String): List<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
    searchSourceBuilder.query(MatchQueryBuilder("name", name))
    val searchRequest = SearchRequest().indices(index).types(type).source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)
    val objectMapper = ObjectMapper()

    return response.hits.map {
      objectMapper.readValue(it.sourceAsString, Package::class.java)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(CustomPackageRepository::class.java)
  }
}
