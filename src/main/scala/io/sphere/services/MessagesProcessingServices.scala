package io.sphere.services

import io.sphere.gateways.CustomObjects.{lastSequenceNumber, newLastSequenceNumber}
import io.sphere.gateways.Login.login
import io.sphere.gateways.Messages.messages
import io.sphere.httpclient.HttpClient
import io.sphere.json._
import io.sphere.models.CustomObjectRestAPI.LastSequenceNumber
import io.sphere.models.MessageRestAPI.{Message, Resource}
import io.sphere.models.OAuthToken

import scala.collection.immutable.Iterable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal


object MessagesProcessingServices {
  type ResourceMessages = (Resource, List[Message])
  type ProcessingResult = Iterable[List[LastSequenceNumber]]

  /**
   * Trigger processing and wait for futures so that main method doesn't return before future completion.
   *
   * @return Number of successfully processed messages
   */
  def processAndWait(implicit client: HttpClient): Int = {
    val processedMessages: ProcessingResult = Await.result(processMessages, Duration(20000, MILLISECONDS))

    processedMessages.map(_.size).sum
  }

  /**
   * Login, get messages and group them by resource.
   *
   */
  def processMessages(implicit client: HttpClient): Future[ProcessingResult] = {
    login().flatMap {
      case Some(token) =>
        implicit val oAuthToken = token
        messages().flatMap {
          messages =>
            Future.sequence {
              messages.groupBy(_.resource).map {
                // skip messages for a resource in case of a failure, e.g. concurrent modification
                // processing is retried on next application run
                processResourceMessages(_).recover { case NonFatal(e) => List.empty }
              }
            }
        }
      case None => Future.successful(List.empty)
    }
  }

  /**
   * Process messages for one single resource, e.g. an order.
   *
   * Get the last succesful processed sequence number and process messages.
   *
   * @param resourceMessages Tuple: (Resource, List[Message])
   * @return a list of successful processed sequence number of messages for a particular resource
   */
  def processResourceMessages(resourceMessages: ResourceMessages)
                             (implicit client: HttpClient, token: OAuthToken): Future[List[LastSequenceNumber]] = {
    val lastSequenceNumberKey: String = s"${resourceMessages._1.typeId}-${resourceMessages._1.id}"
    for {
      maybeLastSequenceNumber <- lastSequenceNumber(lastSequenceNumberKey) if maybeLastSequenceNumber.isDefined
      processedMessages <- processSequence(resourceMessages._2, maybeLastSequenceNumber.get)
    } yield processedMessages
  }

  /**
   * Process a sequence of messages.
   *
   * @return a list of successful processed sequence number of messages for a particular resource.
   */
  def processSequence(messages: List[Message], lastSequenceNumber: LastSequenceNumber)
                     (implicit client: HttpClient, token: OAuthToken): Future[List[LastSequenceNumber]] = {

    // The resulting Future instances are forced to run sequentially
    // to enforce the processing of messages in correct order.
    messages
      .filter(_.sequenceNumber > lastSequenceNumber.value)
      .sortBy(_.sequenceNumber)
      .foldLeft(Future.successful(List(lastSequenceNumber))) {
      case (previousFuture, message) =>
        for {
          sequenceNumbers <- previousFuture
          maybeNext <- newLastSequenceNumber(sequenceNumbers.last, message.sequenceNumber) if maybeNext.isDefined
          processingResult <- Future {
            println(s"\n\nMessage successfully processed: ${toJSON(message)}")
          }
        } yield sequenceNumbers :+ maybeNext.get
      // As the supplied LastSequenceNumber is the first element in the result list it's dropped
    }.map(_.tail)

  }


}
