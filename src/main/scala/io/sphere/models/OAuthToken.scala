package io.sphere.models

import io.sphere.json.JSON
import io.sphere.json.generic._

case class OAuthToken(access_token: String, token_type: String, expires_in: Int, scope: String) {
  val token = access_token
}

object OAuthToken {
  implicit val messageJSON: JSON[OAuthToken] = deriveJSON[OAuthToken]
}

