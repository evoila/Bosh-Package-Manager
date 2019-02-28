package de.evoila.bpm.service

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.Credentials
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import de.evoila.bpm.config.S3Config
import org.springframework.stereotype.Service

@Service
class AmazonS3Service(
    val s3Config: S3Config
) {

  fun getS3Credentials(operation: Operation): Credentials {
    val credentials = makeCreds(operation)
    val stsClient = AWSSecurityTokenServiceClientBuilder
        .standard()
        .withRegion(s3Config.region)
        .withCredentials(AWSStaticCredentialsProvider(credentials))
        .build()
    val sessionTokenRequest = GetSessionTokenRequest()
    sessionTokenRequest.apply {
      durationSeconds = s3Config.expiration
    }
    val result = stsClient.getSessionToken(sessionTokenRequest)

    return result.credentials
  }

  private fun makeCreds(operation: Operation): BasicAWSCredentials {
    val key = operation.name.toLowerCase()

    return s3Config.creds[key]?.let {
      BasicAWSCredentials(it.authKey, it.authSecret)
    }
        ?: throw NoSuchElementException("The needed credentials have not been found. Please specify them in the config yml.")
  }

  fun deleteObject(s3location: String) {
    val s3Client = AmazonS3ClientBuilder.standard()
        .withCredentials(AWSStaticCredentialsProvider(makeCreds(Operation.UPLOAD)))
        .withRegion(s3Config.region)
        .build()
    s3Client.deleteObject(s3Config.bucket, s3location)
  }

  enum class Operation {
    UPLOAD, DOWNLOAD
  }
}