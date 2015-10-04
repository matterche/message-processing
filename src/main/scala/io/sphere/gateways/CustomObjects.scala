package io.sphere.gateways

import dispatch.url
import io.sphere.Config._
import io.sphere.httpclient.HttpClient
import io.sphere.json._
import io.sphere.models.CustomObjectRestAPI.LastSequenceNumber
import io.sphere.models.OAuthToken
import io.sphere.util.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scalaz.{Failure, Success}

class CustomObjects(httpClient: HttpClient) extends Logging {
  private val LastSequenceNumberContainer = io.sphere.Config.lastSuccessfulSequenceContainer

  def newLastSequenceNumber(last: LastSequenceNumber, value: Long)
                           (implicit token: OAuthToken): Future[Option[LastSequenceNumber]] = {
    val newLastSequenceNumber = LastSequenceNumber(
      container = LastSequenceNumberContainer,
      key = last.key,
      value = value,
      version = last.version
    )

    httpClient.api {
      url(ApiUrl + s"/$projectKey/custom-objects")
        .POST
        .setBody(toJSON(newLastSequenceNumber))
    }.map {
      rsp =>
        (rsp.getStatusCode, fromJSON[LastSequenceNumber](rsp.getResponseBody)) match {
          case (200 | 201, Success(lastSequenceNumber)) =>
            Some(lastSequenceNumber)
          case (409, validation) =>
            log.warn(s"Concurrent modification while trying to update " +
              s"last successful sequence number of ${last.key}. Json result: $validation")
            None
          case (status, validation) =>
            log.error(s"Could not create new sequence number custom object. Status $status Json result: $validation")
            None
        }
    }.recover {
      case NonFatal(e) =>
        log.error(s"Could not create new sequence number custom object. $e")
        None
    }
  }

  def lastSequenceNumber(key: String)(implicit token: OAuthToken): Future[Option[LastSequenceNumber]] = {
    httpClient.api {
      url(ApiUrl + s"/$projectKey/custom-objects/$LastSequenceNumberContainer/$key")
    }.map {
      rsp => (rsp.getStatusCode, fromJSON[LastSequenceNumber](rsp.getResponseBody)) match {
        case (200, Success(lastSequenceNumber)) =>
          Some(lastSequenceNumber)
        case (404, Failure(_)) =>
          Some(LastSequenceNumber(container = LastSequenceNumberContainer, key = key, value = 0))
        case (status, validation) =>
          log.error(s"Could not retrieve last sequence number " +
            s"from custom objects of $key. Status $status Json result: $validation")
          None
      }
    }.recover {
      case NonFatal(e) =>
        log.error(s"Could not create new sequence number custom object. $e")
        None
    }
  }
}

object CustomObjects {
  def apply()(implicit httpClient: HttpClient) = new CustomObjects(httpClient)

  def newLastSequenceNumber(last: LastSequenceNumber, value: Long)
                           (implicit h: HttpClient, t: OAuthToken) = apply().newLastSequenceNumber(last, value)

  def lastSequenceNumber(key: String)(implicit h: HttpClient, t: OAuthToken) = apply().lastSequenceNumber(key)
}

