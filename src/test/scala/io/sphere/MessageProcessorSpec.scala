package io.sphere

import io.sphere.fixtures.ResponseMock._
import io.sphere.fixtures.{HttpClientMock, LoginFixtures, MessageFixtures, ResponseMock}
import io.sphere.services.MessagesProcessingServices
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSpec, MustMatchers}


class MessageProcessorSpec
  extends FunSpec
  with MessageFixtures
  with MustMatchers
  with MockitoSugar
  with LoginFixtures {

  describe("MessageProcessor") {

    it("must process 116 messages when starting with sequence number 5 using 166 messages") {
      val routes = token(200, ValidToken) orElse
        messages(200, Messages166) orElse
        lastSuccessfulSequenceNumber(200, "5") orElse
        newSuccessfulSequenceNumber(200)

      implicit val httpClient = HttpClientMock(ResponseMock(routes))

      MessagesProcessingServices.processAndWait must be(116)
    }

    it("must process 166 messages when starting with sequence number 0 using 166 messages") {
      val routes = token(200, ValidToken) orElse
        messages(200, Messages166) orElse
        lastSuccessfulSequenceNumber(200, "0") orElse
        newSuccessfulSequenceNumber(200)

      implicit val httpClient = HttpClientMock(ResponseMock(routes))

      MessagesProcessingServices.processAndWait must be(166)
    }

    it("must process 10 messages when starting with sequence number 0 using 10 messages") {
      val routes = token(200, ValidToken) orElse
        messages(200, Messages43) orElse
        lastSuccessfulSequenceNumber(200, "0") orElse
        newSuccessfulSequenceNumber(200)

      implicit val httpClient = HttpClientMock(ResponseMock(routes))

      MessagesProcessingServices.processAndWait must be(43)
    }

    it("must process all messages when no last successful sequence number can be found and a new one is created") {
      val routes = token(200, ValidToken) orElse
        messages(200, Messages43) orElse
        lastSuccessfulSequenceNumber(404, "invalid") orElse
        newSuccessfulSequenceNumber(201)

      implicit val httpClient = HttpClientMock(ResponseMock(routes))

      MessagesProcessingServices.processAndWait must be(43)
    }

    it("must process 0 messages when creation of new successful sequence numbers leads to 409") {
      val routes = token(200, ValidToken) orElse
        messages(200, Messages43) orElse
        lastSuccessfulSequenceNumber(200, "1") orElse
        newSuccessfulSequenceNumber(409)

      implicit val httpClient = HttpClientMock(ResponseMock(routes))

      MessagesProcessingServices.processAndWait must be(0)
    }

    it("must process 0 messages with unexpected status or response body from API") {
      case class TestFixture(loginStatus: Int,
                             loginAnswer: String,
                             messageStatus: Int,
                             messageAnswer: String,
                             lastSuccessfulStatus: Int,
                             lastSuccessfulAnswer: String,
                             newSuccessfulStatus: Int,
                             clue: String)

      val default = TestFixture(loginStatus = 200,
        loginAnswer = ValidToken,
        messageStatus = 200,
        messageAnswer = Messages43,
        lastSuccessfulStatus = 200,
        lastSuccessfulAnswer = "0",
        newSuccessfulStatus = 200,
        clue = "")

      val fixtures = List[TestFixture](
        default.copy(loginStatus = 400, clue = "with unexpected login status:"),
        default.copy(loginAnswer = "{}", clue = "with unexpected login json:"),
        default.copy(messageStatus = 400, clue = "with unexpected message status:"),
        default.copy(messageAnswer = "{}", clue = "with unexpected message json:"),
        default.copy(lastSuccessfulStatus = 400, clue = "with unexpected custom object status:"),
        default.copy(lastSuccessfulAnswer = "no number", clue = "with unexpected GET custom object answer:"),
        default.copy(newSuccessfulStatus = 400, clue = "with unexpected POST custom object answer:")
      )

      fixtures.foreach {
        fixture =>
          withClue(fixture.clue) {
            val routes = token(fixture.loginStatus, fixture.loginAnswer) orElse
              messages(fixture.messageStatus, fixture.messageAnswer) orElse
              lastSuccessfulSequenceNumber(fixture.lastSuccessfulStatus, fixture.lastSuccessfulAnswer) orElse
              newSuccessfulSequenceNumber(fixture.newSuccessfulStatus)

            implicit val httpClient = HttpClientMock(ResponseMock(routes))

            MessagesProcessingServices.processAndWait must be(0)
          }
      }
    }
  }
}
