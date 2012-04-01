package models.json
import models._
import play.api.libs.json._
import com.mongodb.casbah.Imports._
import java.util.{ Date }
import java.text.SimpleDateFormat

object ComputerJsonFormat {
  implicit def computerJsonFormat = new Format[Computer] {
    val dateFormatter = new SimpleDateFormat( "yyyy-MM-dd" )

    def reads( json: JsValue ): Computer = Computer(
      new ObjectId( json.\( "_id" ).as[String] ),
      json.\( "name" ).as[String],
      safeParseDate( ( json \ "introduced" ).asOpt[String] ),
      safeParseDate( ( json \ "discontinued" ).asOpt[String] ),
      safeReadCompanyId( json.\( "company_id" ).asOpt[String] ) )

    def writes( c: Computer ): JsValue = JsObject( List(
      "_id" -> JsString( c._id.toString ),
      "name" -> JsString( c.name ),
      "introduced" -> JsString( getEditableDate( c.introduced ) ),
      "discontinued" -> JsString( getEditableDate( c.discontinued ) ),
      "company_id" -> JsString( c.company_id.getOrElse( "-" ).toString ) ) )

    private def getEditableDate( date: Option[Date] ): String = date match {
      case Some( d ) => dateFormatter.format( d )
      case None      => ""
    }

    private def safeReadCompanyId( companyId: Option[String] ): Option[ObjectId] = companyId match {
      case Some( id ) if ( !id.trim.isEmpty ) => Some( new ObjectId( id ) )
      case _                                  => None
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