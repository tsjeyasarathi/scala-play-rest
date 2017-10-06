package v1.post

import play.api.libs.json._

case class PostEntity(id: Long, link: String, title: String, body: String)

object PostEntity {
  implicit val postFormat = Json.format[PostEntity]
}
