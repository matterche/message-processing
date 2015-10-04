package io.sphere.fixtures

import dispatch.{HttpExecutor, Req}
import io.sphere.httpclient.HttpClient
import org.mockito.BDDMockito._
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.ExecutionContext

object HttpClientMock extends MockitoSugar {
  def apply(response: ResponseMock) = httpClient(response)

  def httpClient(response: ResponseMock): HttpClient = {
    val httpExecutorMock = mock[HttpExecutor]
    given(httpExecutorMock.apply(any[Req])(any[ExecutionContext])) willAnswer response
    new HttpClient(httpExecutorMock)
  }
}
