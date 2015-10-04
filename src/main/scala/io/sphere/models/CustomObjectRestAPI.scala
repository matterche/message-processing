package io.sphere.models

import io.sphere.json.JSON
import io.sphere.json.generic._

object CustomObjectRestAPI {

  case class LastSequenceNumber(id: Option[String] = None,
                                container: String,
                                key: String,
                                value: Long,
                                version: Option[Long] = None,
                                createdAt: Option[String] = None,
                                lastModifiedAt: Option[String] = None)

  object LastSequenceNumber {
    implicit val messageJSON: JSON[LastSequenceNumber] = deriveJSON[LastSequenceNumber]
  }

}
