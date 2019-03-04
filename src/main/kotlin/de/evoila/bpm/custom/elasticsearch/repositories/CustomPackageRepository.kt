package de.evoila.bpm.custom.elasticsearch.repositories

import de.evoila.bpm.custom.elasticsearch.ElasticSearchRestTemplate
import de.evoila.bpm.entities.Package
import kotlinx.serialization.json.Json
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

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
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_VENDOR + KEYWORD, vendor))
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_NAME + KEYWORD, name))
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_VERSION + KEYWORD, version))
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
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_VENDOR + KEYWORD, vendor))
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_NAME + KEYWORD, name))
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_VERSION + KEYWORD, version))
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

  override fun findById(id: String): Package? {
    val response = requestById(id)

    return if (response.isExists) {
      Json.parse(Package.serializer(), response.sourceAsString)
    } else {
      null
    }
  }

  fun searchPackagesByName(name: String, username: String?): List<Package> {
    val searchPublicSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_NAME, name))
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

  fun searchByVendor(pageable: Pageable, username: String?, vendor: String): Page<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
        .from(pageable.pageNumber)
        .size(pageable.pageSize)
    pageable.sort.forEach {
      searchSourceBuilder.sort(FieldSortBuilder(it.property)
          .order(SortOrder.fromString(it.direction.name)))
    }
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_VENDOR, vendor))
    searchSourceBuilder.query(boolQueryBuilder)
    boolQueryBuilder.must(buildAccessQuery(username))
    val response = elasticSearchRestTemplate.performSearchRequest(
        SearchRequest()
            .indices(index)
            .types(type)
            .source(searchSourceBuilder))

    val content = response.hits.map {
      Json.parse(Package.serializer(), it.sourceAsString)
    }

    return PageImpl(content, pageable, response.hits.totalHits)
  }

  private fun buildAccessQuery(username: String?): BoolQueryBuilder {
    val accessQuery = BoolQueryBuilder()
    accessQuery.should(MatchQueryBuilder(FIELD_ACCESS_LEVEL + KEYWORD, Package.AccessLevel.PUBLIC.name))
    username?.let {
      accessQuery.should(buildPrivate(it))
      accessQuery.should(buildVendor(it))
    }

    return accessQuery
  }

  fun buildPrivate(username: String): BoolQueryBuilder {
    val privateQuery = BoolQueryBuilder()
    privateQuery.must(MatchQueryBuilder(FIELD_ACCESS_LEVEL + KEYWORD, Package.AccessLevel.PRIVATE.name))
    privateQuery.must(MatchQueryBuilder(FIELD_SIGNED_WITH + KEYWORD, username))

    return privateQuery
  }

  fun buildVendor(username: String): BoolQueryBuilder {
    val vendors = vendorRepository.memberOfByName(username)
    val fullVendorQuery = BoolQueryBuilder()
    fullVendorQuery.must(MatchQueryBuilder(FIELD_ACCESS_LEVEL + KEYWORD, Package.AccessLevel.VENDOR.name))
    val vendorQuery = BoolQueryBuilder()
    vendors.forEach { vendorQuery.should(MatchQueryBuilder(FIELD_VENDOR + KEYWORD, it.name)) }
    fullVendorQuery.must(vendorQuery)

    return fullVendorQuery
  }

  companion object {
    private const val FIELD_NAME = "name"
    private const val FIELD_VENDOR = "vendor"
    private const val FIELD_VERSION = "version"
    private const val FIELD_ACCESS_LEVEL = "access_level"
    private const val FIELD_SIGNED_WITH = "signed_with"

    private val log = LoggerFactory.getLogger(CustomPackageRepository::class.java)
  }
}
