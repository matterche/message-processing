package io.sphere

import com.typesafe.config.ConfigFactory


object Config {
  val AuthApiUrl = "https://auth.sphere.io"
  val ApiUrl = "https://api.sphere.io"

  // see configuration file scala / src / main / resources / application.conf
  private val conf = ConfigFactory.load()

  // credentials
  val clientId = conf.getString("message-processing.clientId")
  val clientSecret = conf.getString("message-processing.clientSecret")
  val projectKey = conf.getString("message-processing.projectKey")

  // custom object container for storing last successful sequence number
  val lastSuccessfulSequenceContainer = conf.getString("message-processing.lastSuccessfulSequenceContainer")
}
