package models.json
import models._
import play.api.libs.json._
import com.mongodb.casbah.Imports._

object CompanyJsonFormat {
  implicit def companyJsonFormat = new Format[Company] {
    def reads( json: JsValue ): Company = Company(
      new ObjectId( ( json \ "_id" ).as[String] ),
      ( json \ "name" ).as[String] )

    def writes( c: Company ): JsValue = JsObject( List(
      "_id" -> JsString( c._id.toString ),
      "name" -> JsString( c.name ) ) )
  }
}