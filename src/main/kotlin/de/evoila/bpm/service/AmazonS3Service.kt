package de.evoila.bpm.service

import com.amazonaws.regions.Regions
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.Credentials
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest
import org.springframework.stereotype.Service

@Service
class AmazonS3Service {

  fun generateTemporaryToken(): Credentials {

    val stsClient = AWSSecurityTokenServiceClientBuilder
        .standard()
        .withRegion(Regions.EU_CENTRAL_1)
        .build()

    val sessionTokenRequest = GetSessionTokenRequest()
    sessionTokenRequest.apply {
      durationSeconds = 900
    }

    val result = stsClient.getSessionToken(sessionTokenRequest)

    return result.credentials
  }
}


