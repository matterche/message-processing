package io.sphere.gateways

import java.net.ConnectException

import dispatch.{Future, HttpExecutor, Req}
import io.sphere.gateways.CustomObjects._
import io.sphere.httpclient.HttpClient
import io.sphere.models.CustomObjectRestAPI.LastSequenceNumber
import io.sphere.models.OAuthToken
import org.mockito.BDDMockito._
import org.mockito.Matchers.any
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSpec, MustMatchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class CustomObjectsSpec
  extends FunSpec
  with MustMatchers
  with MockitoSugar {

  describe("Custom Objects Gateway") {

    it("must handle failed futures from http client when getting last successful sequence numbers") {

      val httpExecutorMock = mock[HttpExecutor]
      given(httpExecutorMock.apply(any[Req])(any[ExecutionContext])) willReturn Future.failed(new ConnectException())

      implicit val token = OAuthToken("123", "Bearer", 120, "test")
      implicit val httpClient = new HttpClient(httpExecutorMock)
      val lastNumber: Option[LastSequenceNumber] = Await.result(lastSequenceNumber(""), Duration.Inf)

      lastNumber must be(None)
    }

    it("must handle failed futures from http client when creating new successful sequence numbers") {

      val httpExecutorMock = mock[HttpExecutor]
      given(httpExecutorMock.apply(any[Req])(any[ExecutionContext])) willReturn Future.failed(new ConnectException())

      implicit val token = OAuthToken("123", "Bearer", 120, "test")
      implicit val httpClient = new HttpClient(httpExecutorMock)
      val sequenceNumberMock = LastSequenceNumber(container = "", key = "", value = 4)
      val lastNumber: Option[LastSequenceNumber] = Await.result(newLastSequenceNumber(sequenceNumberMock, 5), Duration.Inf)

      lastNumber must be(None)
    }

  }
}
