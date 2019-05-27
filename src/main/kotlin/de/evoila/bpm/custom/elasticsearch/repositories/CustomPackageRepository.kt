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
    val publisherRepository: CustomPublisherRepository
) : AbstractElasticSearchRepository<Package>(
    elasticSearchRestTemplate
) {

  override val index: String = "packages"

  override fun serializeObject(entity: Package): String = Json.stringify(Package.serializer(), entity)

  fun save(entity: Package): Package = super.save(entity, VERSION_PIPELINE)

  fun findByPublisherAndNameAndVersion(publisher: String, name: String, version: String): Package? {
    val searchSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_PUBLISHER + KEYWORD, publisher))
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

  fun getNewest(publisher: String, name: String, username: String?): Package? {
    val searchSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_PUBLISHER + KEYWORD, publisher))
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_NAME + KEYWORD, name))
    boolQueryBuilder.must(buildAccessQuery(username))
    searchSourceBuilder.query(boolQueryBuilder).size(1)
    searchSourceBuilder.sort(FieldSortBuilder(SEMVER_MAJOR + KEYWORD).order(SortOrder.DESC))
    searchSourceBuilder.sort(FieldSortBuilder(SEMVER_MINOR + KEYWORD).order(SortOrder.DESC))
    searchSourceBuilder.sort(FieldSortBuilder(SEMVER_PATCH + KEYWORD).order(SortOrder.DESC))
    val searchRequest = SearchRequest().indices(index).types(type)
        .source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    if (response.hits.totalHits > 1) {
      log.info("Multiple Hits!!!!")
      response.hits.forEach {
        log.info(it.toString())
      }
    }

    return response.hits.map { Json(strictMode = false).parse(Package.serializer(), it.sourceAsString) }.firstOrNull()
  }

  fun getNewest(publisher: String, name: String, major: String, username: String?): Package? {
    val searchSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_PUBLISHER + KEYWORD, publisher))
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_NAME + KEYWORD, name))
    boolQueryBuilder.must(MatchQueryBuilder(SEMVER_MAJOR + KEYWORD, major))
    boolQueryBuilder.must(buildAccessQuery(username))
    searchSourceBuilder.query(boolQueryBuilder).size(1)
    searchSourceBuilder.sort(FieldSortBuilder(SEMVER_MINOR + KEYWORD).order(SortOrder.DESC))
    searchSourceBuilder.sort(FieldSortBuilder(SEMVER_PATCH + KEYWORD).order(SortOrder.DESC))
    val searchRequest = SearchRequest().indices(index).types(type)
        .source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    if (response.hits.totalHits > 1) {
      log.info("Multiple Hits!!!!")
      response.hits.forEach {
        log.info(it.toString())
      }
    }

    return response.hits.map { Json(strictMode = false).parse(Package.serializer(), it.sourceAsString) }.firstOrNull()
  }

  fun getNewest(publisher: String, name: String, major: String, minor: String, username: String?): Package? {
    val searchSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_PUBLISHER + KEYWORD, publisher))
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_NAME + KEYWORD, name))
    boolQueryBuilder.must(MatchQueryBuilder(SEMVER_MAJOR + KEYWORD, major))
    boolQueryBuilder.must(MatchQueryBuilder(SEMVER_MINOR + KEYWORD, minor))
    boolQueryBuilder.must(buildAccessQuery(username))
    searchSourceBuilder.query(boolQueryBuilder).size(1)
    searchSourceBuilder.sort(FieldSortBuilder(SEMVER_PATCH + KEYWORD).order(SortOrder.DESC))
    val searchRequest = SearchRequest().indices(index).types(type)
        .source(searchSourceBuilder)
    val response = elasticSearchRestTemplate.performSearchRequest(searchRequest)

    if (response.hits.totalHits > 1) {
      log.info("Multiple Hits!!!!")
      response.hits.forEach {
        log.info(it.toString())
      }
    }

    return response.hits.map { Json(strictMode = false).parse(Package.serializer(), it.sourceAsString) }.firstOrNull()
  }

  fun findByPublisherAndNameAndVersionAuth(publisher: String, name: String, version: String, username: String?): Package? {
    val searchSourceBuilder = SearchSourceBuilder()
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_PUBLISHER + KEYWORD, publisher))
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

    return response.hits.map { Json(strictMode = false).parse(Package.serializer(), it.sourceAsString) }.firstOrNull()
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
      Json(strictMode = false).parse(Package.serializer(), it.sourceAsString)
    }

    return PageImpl(content, pageable, response.hits.totalHits)
  }

  override fun findById(id: String): Package? {
    val response = requestById(id)

    return if (response.isExists) {
      Json(strictMode = false).parse(Package.serializer(), response.sourceAsString)
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
      Json(strictMode = false).parse(Package.serializer(), it.sourceAsString)
    }
  }

  fun searchByPublisher(pageable: Pageable, username: String?, publisher: String): Page<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
        .from(pageable.pageNumber)
        .size(pageable.pageSize)
    pageable.sort.forEach {
      searchSourceBuilder.sort(FieldSortBuilder(it.property)
          .order(SortOrder.fromString(it.direction.name)))
    }
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_PUBLISHER, publisher))
    searchSourceBuilder.query(boolQueryBuilder)
    boolQueryBuilder.must(buildAccessQuery(username))
    val response = elasticSearchRestTemplate.performSearchRequest(
        SearchRequest()
            .indices(index)
            .types(type)
            .source(searchSourceBuilder))

    val content = response.hits.map {
      Json(strictMode = false).parse(Package.serializer(), it.sourceAsString)
    }

    return PageImpl(content, pageable, response.hits.totalHits)
  }

  fun searchByPublisherAndName(pageable: Pageable, username: String?, publisher: String, name: String): Page<Package> {
    val searchSourceBuilder = SearchSourceBuilder()
        .from(pageable.pageNumber)
        .size(pageable.pageSize)
    pageable.sort.forEach {
      searchSourceBuilder.sort(FieldSortBuilder(it.property)
          .order(SortOrder.fromString(it.direction.name)))
    }
    val boolQueryBuilder = BoolQueryBuilder()
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_PUBLISHER, publisher))
    boolQueryBuilder.must(MatchQueryBuilder(FIELD_NAME, name))
    searchSourceBuilder.query(boolQueryBuilder)
    boolQueryBuilder.must(buildAccessQuery(username))
    val response = elasticSearchRestTemplate.performSearchRequest(
        SearchRequest()
            .indices(index)
            .types(type)
            .source(searchSourceBuilder))
    val content = response.hits.map {
      Json(strictMode = false).parse(Package.serializer(), it.sourceAsString)
    }

    return PageImpl(content, pageable, response.hits.totalHits)
  }

  private fun buildAccessQuery(username: String?): BoolQueryBuilder {
    val accessQuery = BoolQueryBuilder()
    accessQuery.should(MatchQueryBuilder(FIELD_ACCESS_LEVEL + KEYWORD, Package.AccessLevel.PUBLIC.name))
    username?.let {
      accessQuery.should(buildPrivate(it))
      accessQuery.should(buildPublisherQuery(it))
    }

    return accessQuery
  }

  fun buildPrivate(username: String): BoolQueryBuilder {
    val privateQuery = BoolQueryBuilder()
    privateQuery.must(MatchQueryBuilder(FIELD_ACCESS_LEVEL + KEYWORD, Package.AccessLevel.PRIVATE.name))
    privateQuery.must(MatchQueryBuilder(FIELD_SIGNED_WITH + KEYWORD, username))

    return privateQuery
  }

  fun buildPublisherQuery(username: String): BoolQueryBuilder {
    val publishers = publisherRepository.memberOfByName(username)
    val fullPublisherQuery = BoolQueryBuilder()
    fullPublisherQuery.must(MatchQueryBuilder(FIELD_ACCESS_LEVEL + KEYWORD, Package.AccessLevel.PUBLISHER.name))
    val publisherQuery = BoolQueryBuilder()
    publishers.forEach { publisherQuery.should(MatchQueryBuilder(FIELD_PUBLISHER + KEYWORD, it.name)) }
    fullPublisherQuery.must(publisherQuery)

    return fullPublisherQuery
  }

  companion object {
    private const val FIELD_NAME = "name"
    private const val FIELD_PUBLISHER = "publisher"
    private const val FIELD_VERSION = "version"
    private const val FIELD_ACCESS_LEVEL = "access_level"
    private const val FIELD_SIGNED_WITH = "signed_with"
    private const val VERSION_PIPELINE = "version-pipeline"
    private const val SEMVER_MAJOR = "major"
    private const val SEMVER_MINOR = "minor"
    private const val SEMVER_PATCH = "patch"

    private val log = LoggerFactory.getLogger(CustomPackageRepository::class.java)
  }
}
