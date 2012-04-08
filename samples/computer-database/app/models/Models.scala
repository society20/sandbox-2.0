package models

import java.util.{ Date }
import play.api.db._
import play.api.Play.current
import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import java.util.regex.Pattern
import com.codahale.jerkson.JsonSnakeCase

case class Company( _id: ObjectId, name: String, @transient fake: Boolean = false )
case class Computer( _id: ObjectId, name: String, introduced: Option[Date], discontinued: Option[Date],
                     company: Option[Company] )

object Company {
  def options: Seq[( String, String )] = {
    CompanyDAO.findAll.foldLeft( Seq.empty[( String, String )] )(
      ( seq, company ) => seq :+ ( company._id.toString, company.name ) )
  }
}

object CompanyDAO extends SalatDAO[Company, ObjectId]( collection = MongoConnection()( "cdb" )( "companies" ) ) {
  val mongo = MongoConnection()( "cdb" )( "companies" )
  def findAll = CompanyDAO.find( MongoDBObject.empty )
}

object ComputerDAO extends SalatDAO[Computer, ObjectId]( collection = MongoConnection()( "cdb" )( "computers" ) ) {

  val mongo = MongoConnection()( "cdb" )( "computers" )

  def list( page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "" ): Page[Computer] = {
    val offset = page * pageSize
    val regex = Pattern.compile( filter, Pattern.CASE_INSENSITIVE )
    val query = MongoDBObject( "name" -> regex )
    val all = find( query )
    val results = all
      .skip( offset )
      .limit( pageSize )
      .sort( MongoDBObject( geSort( orderBy ) ) )

    Page( results.toSeq, page, offset, all.count )
  }

  def jsonListWithCount( page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "" ): ( Int, Seq[( Computer )] ) = {
    val offset = page * pageSize
    val regex = Pattern.compile( filter, Pattern.CASE_INSENSITIVE )
    val query = MongoDBObject( "name" -> regex )
    val all = find( query )
    val count = all.count

    val results = all
      .skip( offset )
      .limit( pageSize )
      .sort( MongoDBObject( geSort( orderBy ) ) )

    ( count, results.toSeq )
  }
  def findById( id: String ): Option[Computer] = {
    findOneByID( new ObjectId( id ) )
  }
  def geSort( orderBy: Int ): ( String, Int ) = {
    val columnNames = Seq( "_id", "name", "introduced", "discontinued", "company_id" )
    val columnNumber = scala.math.abs( orderBy ) - 1
    val columnName = columnNames( columnNumber )
    val sort: Int = orderBy / scala.math.abs( orderBy )
    ( columnName, sort )
  }
  def main( args: Array[String] ) {
    println( geSort( 2 ) )
    println( geSort( -2 ) )
    println( geSort( 3 ) )
    println( geSort( -3 ) )
  }
}

/**
 * Helper for pagination.
 */
case class Page[A]( items: Seq[A], page: Int, offset: Long, total: Long ) {
  lazy val prev = Option( page - 1 ).filter( _ >= 0 )
  lazy val next = Option( page + 1 ).filter( _ => ( offset + items.size ) < total )
}

