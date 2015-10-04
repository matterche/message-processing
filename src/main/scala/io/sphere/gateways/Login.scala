package io.sphere.gateways


import com.ning.http.util.Base64
import dispatch.{Future, url}
import io.sphere.Config.AuthApiUrl
import io.sphere.httpclient.HttpClient
import io.sphere.json._
import io.sphere.models.OAuthToken
import io.sphere.util.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal
import scalaz.Success

class Login(httpClient: HttpClient) extends Logging {

  def login(clientId: String, clientSecret: String, projectKey: String): Future[Option[OAuthToken]] = {
    val encoded = Base64.encode(s"$clientId:$clientSecret".getBytes)

    httpClient.http {
      url(AuthApiUrl + "/oauth/token").POST
        .setHeader("Authorization", s"Basic $encoded")
        .setHeader("Content-Type", "application/x-www-form-urlencoded")
        .setBody(s"grant_type=client_credentials&scope=manage_project:$projectKey")
    }.map {
      rsp =>
        (rsp.getStatusCode, fromJSON[OAuthToken](rsp.getResponseBody)) match {
          case (200, Success(token: OAuthToken)) => Some(token)
          case (status, validation) =>
            log.error(s"Could not retrieve OAuth token. Status $status Json result: $validation")
            None
        }
    }.recover {
      case NonFatal(e) =>
        log.error(s"Could not retrieve OAuth token. $e")
        None
    }
  }
}


object Login {
  def login()(implicit httpClient: HttpClient): Future[Option[OAuthToken]] = {
    import io.sphere.Config.{clientId, clientSecret, projectKey}
    new Login(httpClient).login(clientId, clientSecret, projectKey)
  }
}