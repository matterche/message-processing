package io.sphere.gateways

import java.net.ConnectException

import dispatch.{Future, HttpExecutor, Req}
import io.sphere.fixtures.MessageFixtures
import io.sphere.gateways.Messages.messages
import io.sphere.httpclient.HttpClient
import io.sphere.models.MessageRestAPI.Message
import io.sphere.models.OAuthToken
import org.mockito.BDDMockito._
import org.mockito.Matchers.any
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSpec, MustMatchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class MessagesSpec
  extends FunSpec
  with MessageFixtures
  with MustMatchers
  with MockitoSugar {


  describe("Messages Gateway") {
    it("must handle failed futures from http client") {
      val httpExecutorMock = mock[HttpExecutor]
      given(httpExecutorMock.apply(any[Req])(any[ExecutionContext])) willReturn Future.failed(new ConnectException())

      implicit val token = OAuthToken("123", "Bearer", 120, "test")
      implicit val httpClient = new HttpClient(httpExecutorMock)
      val messagesResult: List[Message] = Await.result(messages(), Duration.Inf)

      messagesResult must be(List.empty[Message])
    }
  }

}
