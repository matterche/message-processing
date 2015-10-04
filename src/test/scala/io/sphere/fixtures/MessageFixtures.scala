package io.sphere.fixtures

import scala.io.Source

trait MessageFixtures {
  val MessageTypes = List(
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

  val SingleMessage =
    """
      |{
      |  "offset": 0,
      |  "count": 1,
      |  "total": 1,
      |  "results": [
      |    {
      |      "id": "793a8a14-5caa-46e0-b1d0-ec951c2227cc",
      |      "version": 1,
      |      "sequenceNumber": 10,
      |      "resource": {
      |        "typeId": "order",
      |        "id": "8fb43c84-dced-43da-b444-bcc3256c6f50"
      |      },
      |      "resourceVersion": 11,
      |      "orderId": "8fb43c84-dced-43da-b444-bcc3256c6f50",
      |      "orderState": "Open",
      |      "type": "OrderStateChanged",
      |      "createdAt": "2015-09-28T08:24:54.390Z",
      |      "lastModifiedAt": "2015-09-28T08:24:54.390Z"
      |    }
      |  ]
      |}
    """.stripMargin

  val Messages166 = Source.fromFile("src/test/scala/io/sphere/fixtures/166-messages.json").mkString
  val Messages43 = Source.fromFile("src/test/scala/io/sphere/fixtures/43-messages.json").mkString
}
