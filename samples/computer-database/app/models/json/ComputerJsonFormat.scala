package models.json
import models._
import play.api.libs.json._
import com.mongodb.casbah.Imports._
import java.util.{ Date }
import java.text.SimpleDateFormat
import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao._
import com.mongodb.util.JSONParser
import CompanyJsonFormat._
import play.api.libs.json.Json._

object ComputerJsonFormat {
  implicit def computerJsonFormat = new Format[Computer] {
    val dateFormatter = new SimpleDateFormat( "yyyy-MM-dd" )

    def reads( json: JsValue ): Computer = {
      Computer(
        new ObjectId( ( json \ "_id" ).as[String] ),
        ( json \ "name" ).as[String],
        safeParseDate( ( json \ "introduced" ).asOpt[String] ),
        safeParseDate( ( json \ "discontinued" ).asOpt[String] ),
        ( json \ "company" ).asOpt[Company] )
    }

    def writes( c: Computer ): JsValue = {
      JsObject( List(
        "_id" -> JsString( c._id.toString ),
        "name" -> JsString( c.name ),
        "introduced" -> getEditableDate( c.introduced ),
        "discontinued" -> getEditableDate( c.discontinued ),
        "company" -> toJson( c.company ) ) )
    }
    private def getEditableDate( date: Option[Date] ): JsValue = date match {
      case Some( d ) => JsString( dateFormatter.format( d ) )
      case None      => JsNull
    }

    private def safeParseDate( input: Option[String] ): Option[Date] = input match {
      case Some( str ) => {
        try {
          val parsedDate = dateFormatter parse str
          Some( parsedDate )
        }
        catch { case e: Exception => None }
      }
      case _ => None
    }
  }
}