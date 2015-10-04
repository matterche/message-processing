package io.sphere

import com.ning.http.client.AsyncHttpClient
import dispatch.Http
import io.sphere.httpclient.HttpClient
import io.sphere.httpclient.HttpClient.withHttpClient
import io.sphere.services.MessagesProcessingServices._
import io.sphere.util.Logging

import scala.annotation.tailrec

object Application extends App with Logging {

  val httpClient = new HttpClient(new Http(new AsyncHttpClient))

  withHttpClient(httpClient) {
    httpClient =>
      scheduleMessageProcessing(httpClient)
  }


  @tailrec
  def scheduleMessageProcessing(implicit client: HttpClient): Unit = {
    println {
      processAndWait match {
        case nrProcessedMessages if nrProcessedMessages == 0 => s"\n\n\nNo new message processed\n\n\n"
        case nrProcessedMessages => s"\n\n\nSuccessfully processed $nrProcessedMessages message(s)\n\n\n"
      }
    }

    scheduleMessageProcessing
  }

}