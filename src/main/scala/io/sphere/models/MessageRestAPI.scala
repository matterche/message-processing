package io.sphere.models

import io.sphere.json.JSON
import io.sphere.json.generic._

object MessageRestAPI {

  case class MessageResult(count: Option[Int], total: Int, offset: Int, results: List[Message])

  object MessageResult {
    implicit val messageJSON: JSON[MessageResult] = deriveJSON[MessageResult]
  }

  case class Message(id: String, sequenceNumber: Long, resource: Resource, resourceVersion: Long, `type`: String, createdAt: String)

  object Message {
    implicit val messageJSON: JSON[Message] = deriveJSON[Message]
  }

  case class Resource(typeId: String, id: String)

  object Resource {
    implicit val resourceJSON: JSON[Resource] = deriveJSON[Resource]
  }

}
