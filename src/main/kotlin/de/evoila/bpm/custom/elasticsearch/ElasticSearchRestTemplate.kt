package de.evoila.bpm.custom.elasticsearch

import de.evoila.bpm.config.ElasticSearchConfig
import org.apache.http.HttpHost
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.stereotype.Service

@Service
class ElasticSearchRestTemplate(
    val elasticSearchConfig: ElasticSearchConfig
) {

  private fun buildClient(): RestHighLevelClient = RestHighLevelClient(
      RestClient.builder(
          HttpHost(elasticSearchConfig.host, elasticSearchConfig.port, "http")
      )
  )

  fun performSearchRequest(request: SearchRequest): SearchResponse {
    val client = buildClient()
    val response = client.search(request, RequestOptions.DEFAULT)
    client.close()

    return response
  }

  fun performIndexRequest(request: IndexRequest): IndexResponse {
    val client = buildClient()
    val response = client.index(request, RequestOptions.DEFAULT)
    client.close()

    return response
  }

  fun performGetRequest(request: GetRequest): GetResponse {
    val client = buildClient()
    val response = client.get(request, RequestOptions.DEFAULT)
    client.close()

    return response
  }

  fun performDeleteRequest(request: DeleteRequest): DeleteResponse {
    val client = buildClient()
    val response = client.delete(request, RequestOptions.DEFAULT)
    client.close()

    return response
  }
}
