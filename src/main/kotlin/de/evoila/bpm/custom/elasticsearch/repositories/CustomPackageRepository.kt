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
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CustomPackageRepository(
    elasticSearchRestTemplate: ElasticSearchRestTemplate,
    val vendorRepository: CustomVendorRepository
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
    boolQueryBuilder.must(MatchQueryBuilder("vendor_keyword", vendor))
    boolQueryBuilder.must(MatchQueryBuilder("name_keyword", name))
    boolQueryBuilder.must(MatchQueryBuilder("version_keyword", version))
    searchSourceBuilder.query(boolQueryBuilder).size(1)

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


  fun findByVendorAndNameAndVersion(vendor: String, name: String, version: String, username: String?): Package? {

    val searchSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder("vendor_keyword", vendor))
    boolQueryBuilder.must(MatchQueryBuilder("name_keyword", name))
    boolQueryBuilder.must(MatchQueryBuilder("version_keyword", version))
    boolQueryBuilder.must(buildAccessQuery(username))

    searchSourceBuilder.query(boolQueryBuilder).size(1)


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

  fun findAll(pageable: Pageable, username: String?): Page<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
        .from(pageable.pageNumber)
        .size(pageable.pageSize)

    searchSourceBuilder.query(BoolQueryBuilder().must(buildAccessQuery(username)))

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

  override fun findById(id: String): Optional<Package> {
    val response = requestById(id)

    return if (response.isExists) {
      val result = Json.parse(Package.serializer(), response.sourceAsString)

      Optional.of(result)
    } else {
      Optional.empty()
    }
  }

  fun getPackagesByName(name: String, username: String?): List<Package> {

    val searchPublicSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder("name", name))
    searchPublicSourceBuilder.query(boolQueryBuilder)
    boolQueryBuilder.must(buildAccessQuery(username))

    val response = elasticSearchRestTemplate.performSearchRequest(
        SearchRequest()
            .indices(index)
            .types(type)
            .source(searchPublicSourceBuilder)
    )

    return response.hits.map {
      Json.parse(Package.serializer(), it.sourceAsString)
    }
  }

  private fun buildAccessQuery(username: String?): BoolQueryBuilder {
    val accessQuery = BoolQueryBuilder()
    accessQuery.should(MatchQueryBuilder("access_level_keyword", "PUBLIC"))

    username?.let {
      accessQuery.should(buildPrivate(it))
      accessQuery.should(buildVendor(it))
    }
    return accessQuery
  }

  fun buildPrivate(username: String): BoolQueryBuilder {
    val privateQuery = BoolQueryBuilder()
    privateQuery.must(MatchQueryBuilder("access_level_keyword", "PRIVATE"))
    privateQuery.must(MatchQueryBuilder("signed_with_keyword", username))

    return privateQuery
  }

  fun buildVendor(username: String): BoolQueryBuilder {
    val vendors = vendorRepository.memberOfByName(username)
    val fullVendorQuery = BoolQueryBuilder()
    fullVendorQuery.must(MatchQueryBuilder("access_level_keyword", "VENDOR"))
    val vendorQuery = BoolQueryBuilder()
    vendors.forEach { vendorQuery.should(MatchQueryBuilder("vendor_keyword", it.name)) }
    fullVendorQuery.must(vendorQuery)

    return fullVendorQuery
  }

  companion object {
    private val log = LoggerFactory.getLogger(CustomPackageRepository::class.java)
  }
}
