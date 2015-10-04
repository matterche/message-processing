package io.sphere.fixtures

import com.ning.http.client.Response
import dispatch.{Future, Req}
import io.sphere.fixtures.ResponseMock.Routes
import org.mockito.BDDMockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock

class ResponseMock(withRoutes: Routes) extends Answer[Future[Response]] {
  override def answer(invocation: InvocationOnMock) = {
    val arguments: Array[AnyRef] = invocation.getArguments
    if (withRoutes.isDefinedAt(arguments.head))
      Future.successful(withRoutes.apply(arguments.head))
    else
      throw new Exception(s"no route defined for ${arguments.head}")
  }
}

object ResponseMock extends mock.MockitoSugar {
  type Routes = PartialFunction[AnyRef, Response]

  def apply(withRoutes: Routes) = new ResponseMock(withRoutes)

  def messages(status: Int, body: String): Routes = {
    case r: Req if r.toRequest.getOriginalURI.toString.endsWith("/messages") =>
      response(status, body)
  }

  def token(status: Int, body: String): Routes = {
    case r: Req if r.toRequest.getOriginalURI.toString.endsWith("/oauth/token") =>
      response(status, body)
  }

  def lastSuccessfulSequenceNumber(status: Int, lastSuccessfulNumber: String): Routes = {
    case r: Req if r.toRequest.getOriginalURI.toString.contains("/custom-objects") && "GET" == r.toRequest.getMethod =>
      val orderKey = r.toRequest.getOriginalURI.toString.split("/").takeRight(1)(0)
      val body = s"""{"container":"orderMessages.lastSequenceNumber","key":"${orderKey.toString}","value":$lastSuccessfulNumber}"""
      response(status, body)
  }

  def newSuccessfulSequenceNumber(status: Int): Routes = {
    case r: Req if r.toRequest.getOriginalURI.toString.contains("/custom-objects")
      && "POST" == r.toRequest.getMethod =>
      val responseMock = mock[Response]
      given(responseMock.getResponseBody) willReturn r.toRequest.getStringData
      given(responseMock.getStatusCode) willReturn status
      responseMock
  }

  private def response(status: Int, body: String): Response = {
    val responseMock = mock[Response]
    given(responseMock.getStatusCode) willReturn status
    given(responseMock.getResponseBody) willReturn body
    responseMock
  }
}