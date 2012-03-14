package models.form.format

import com.mongodb.casbah.Imports._

import models._
import play.api.data.format.Formatter
import play.api.data._

object FormatsObjectId {
  implicit def objectId = new Formatter[ObjectId] {

    def bind( key: String, data: Map[String, String] ) = {
      data.get( key )
        .toRight( Seq( FormError( key, "error.required", Nil ) ) )
        .right.map( value => new ObjectId( value ) )

    }
    def unbind( key: String, value: ObjectId ) = Map( key -> value.toString )
  }
}