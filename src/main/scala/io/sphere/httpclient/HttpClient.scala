package io.sphere.httpclient

import com.ning.http.client.Response
import dispatch._
import io.sphere.models.OAuthToken

import scala.concurrent.ExecutionContext.Implicits.global

class HttpClient(executor: HttpExecutor) {
  private[this] val httpExecutor = executor

  def shutdown(): Unit = httpExecutor.shutdown()

  def http(req: Req): Future[Response] = httpExecutor(req)

  def api(req: Req)(implicit token: OAuthToken): Future[Response] = {
    httpExecutor(
      req.setHeader("Authorization", s"Bearer ${token.access_token}")
        .setContentType("application/json", "UTF-8")
    )
  }

}

object HttpClient {

  def withHttpClient[T](httpClient: HttpClient)(handler: HttpClient => T): T = {
    try {
      handler(httpClient)
    } finally {
      httpClient.shutdown()
    }
  }
}

