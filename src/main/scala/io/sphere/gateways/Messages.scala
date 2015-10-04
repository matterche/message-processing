package io.sphere.gateways

import dispatch.url
import io.sphere.Config._
import io.sphere.httpclient.HttpClient
import io.sphere.json._
import io.sphere.models.MessageRestAPI.{Message, MessageResult}
import io.sphere.models.OAuthToken
import io.sphere.util.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scalaz.Success

class Messages(httpClient: HttpClient) extends Logging {
  private val MessageTypes = List(
    "LineItemStateTransition",
    "CustomLineItemStateTransition",
    "DeliveryAdded",
    "ParcelAddedToDelivery",
    "ReturnInfoAdded",
    "OrderCreated",
    "OrderImported",
    "OrderStateChanged",
    "OrderStateTransition"
  )

  private val TypeQuery = s"type in (${MessageTypes.map(t => s""""$t"""").mkString(",")})"

  private lazy val NoMessages = List.empty[Message]

  def getMessages()(implicit token: OAuthToken): Future[List[Message]] = {
    httpClient.api {
      url(ApiUrl + s"/$projectKey/messages")
        .addQueryParameter("limit", "0")
        .addQueryParameter("sort", "createdAt")
        .addQueryParameter("where", TypeQuery)
    }.map {
      rsp =>
        (rsp.getStatusCode, fromJSON[MessageResult](rsp.getResponseBody)) match {
          case (200, Success(messages)) => messages.results
          case (status, validation) =>
            log.error(s"Could no retrieve messages. Status $status Json result: $validation")
            NoMessages
        }
    }.recover {
      case NonFatal(e) =>
        log.error(s"Could not retrieve messages. $e")
        NoMessages
    }
  }
}

object Messages {
  def messages()(implicit httpClient: HttpClient, token: OAuthToken) = new Messages(httpClient).getMessages()
}