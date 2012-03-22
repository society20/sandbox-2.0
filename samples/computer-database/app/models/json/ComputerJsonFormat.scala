package models.json
import models._
import play.api.libs.json._
import com.mongodb.casbah.Imports._
import java.util.{ Date }
import java.text.SimpleDateFormat

object ComputerJsonFormat {
  implicit def computerJsonFormat = new Format[Computer] {
    val dateFromatter = new SimpleDateFormat( "yyy-MM-dd" )
    def parseDate( input: Option[String] ) = input.map( string => try { dateFromatter parse string } catch { case e: IllegalArgumentException => null } )
    def reads( json: JsValue ): Computer = Computer(
      new ObjectId( ( json \ "_id" ).as[String] ),
      ( json \ "name" ).as[String],
      parseDate( ( json \ "introduced" ).asOpt[String] ),
      parseDate( ( json \ "discontinued" ).asOpt[String] ),
      ( json \ "company_id" ).asOpt[String].map( company_id => new ObjectId( company_id ) ) )

    def writes( c: Computer ): JsValue = JsObject( List(
      "_id" -> JsString( c._id.toString ),
      "name" -> JsString( c.name ),
      "introduced" -> JsString( c.introduced.getOrElse( "" ).toString ),
      "discontinued" -> JsString( c.discontinued.getOrElse( "" ).toString ),
      "company_id" -> JsString( c.company_id.getOrElse( "" ).toString ) ) )

  }
}