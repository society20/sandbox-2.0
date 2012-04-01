package models

import java.util.{ Date }
import play.api.db._
import play.api.Play.current
import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import java.util.regex.Pattern

case class Company( _id: ObjectId, name: String )
case class Computer( _id: ObjectId, name: String, introduced: Option[Date], discontinued: Option[Date],
                     //TODO : try to switch from ref to embedded while changing the form and the template
                     company_id: Option[ObjectId] )

object Computer {
  def compose( id: String, computer: Computer ): Computer = {
    new Computer( new ObjectId( id ), computer.name, computer.introduced, computer.discontinued, computer.company_id )
  }
}

object Company {
  def options: Seq[( String, String )] = {
    CompanyDAO.findAll.foldLeft( Seq.empty[( String, String )] )(
      ( seq, company ) => seq :+ ( company._id.toString, company.name ) )
  }
}

object CompanyDAO extends SalatDAO[Company, ObjectId]( collection = MongoConnection()( "cdb" )( "companies" ) ) {
  val mongo = MongoConnection()( "cdb" )( "companies" )
  def findAll = CompanyDAO.find( MongoDBObject.empty )
  def getCompany( computer: Computer ): Option[Company] = {
    computer.company_id match {
      case Some( companyId ) => findOneByID( companyId )
      case _                 => None
    }
  }
}

object ComputerDAO extends SalatDAO[Computer, ObjectId]( collection = MongoConnection()( "cdb" )( "computers" ) ) {

  val mongo = MongoConnection()( "cdb" )( "computers" )

  def list( page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "" ): Page[( Computer, Option[Company] )] = {
    val offset = page * pageSize
    val regex = Pattern.compile( filter, Pattern.CASE_INSENSITIVE )
    val query = MongoDBObject( "name" -> regex )
    val all = find( query )
    val results = all
      .skip( offset )
      .limit( pageSize )
      .sort( MongoDBObject( geSort( orderBy ) ) )
    val computersCompanySeq = results.foldLeft( Seq.empty[( Computer, Option[Company] )] )(
      ( seq, computer ) => seq :+ ( computer, CompanyDAO.getCompany( computer ) ) ) // TODO : query the db for company

    Page( computersCompanySeq, page, offset, all.count )
  }

  def jsonListWithCount( page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "" ): ( Int, Seq[( Computer, Option[Company] )] ) = {
    val offset = page * pageSize
    val regex = Pattern.compile( filter, Pattern.CASE_INSENSITIVE )
    val query = MongoDBObject( "name" -> regex )
    val all = find( query )
    val count = all.count
    val results = all
      .skip( offset )
      .limit( pageSize )
      .sort( MongoDBObject( geSort( orderBy ) ) )
    (
      count,
      results.foldLeft( Seq.empty[( Computer, Option[Company] )] )(
        ( seq, computer ) => seq :+ ( computer, CompanyDAO.getCompany( computer ) ) ) )
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

