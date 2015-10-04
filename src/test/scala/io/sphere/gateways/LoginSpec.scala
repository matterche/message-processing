package io.sphere.gateways

import java.net.ConnectException

import dispatch.{Future, HttpExecutor, Req}
import io.sphere.gateways.Login.login
import io.sphere.httpclient.HttpClient
import io.sphere.models.OAuthToken
import org.mockito.BDDMockito._
import org.mockito.Matchers.any
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSpec, MustMatchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class LoginSpec
  extends FunSpec
  with MustMatchers
  with MockitoSugar {

  describe("Login Gateway") {
    it("must handle failed futures from http client") {
      val httpExecutorMock = mock[HttpExecutor]
      given(httpExecutorMock.apply(any[Req])(any[ExecutionContext])) willReturn Future.failed(new ConnectException())

      implicit val httpClient = new HttpClient(httpExecutorMock)
      val token: Option[OAuthToken] = Await.result(login(), Duration.Inf)

      token must be(None)
    }
  }

}
