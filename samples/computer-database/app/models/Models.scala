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
    new Computer( new ObjectId(id), computer.name, computer.introduced, computer.discontinued, computer.company_id )
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

//
//  // -- Parsers
//
//  //  /**
//  //   * Parse a (Computer,Company) from a ResultSet
//  //   */
//  //  val withCompany = Computer.simple ~ (Company.simple ?) map {
//  //    case computer~company => (computer,company)
//  //  }
//
//  // -- Queries
//
//  /**
//   * Retrieve a computer from the id.
//   */
//  def findById(id: Long): Option[Computer] = {
//    //    DB.withConnection { implicit connection =>
//    //      SQL("select * from computer where id = {id}").on('id -> id).as(Computer.simple.singleOpt)
//    //    }
//    None
//  }
//
//  /**
//   * Return a page of (Computer,Company).
//   *
//   * @param page Page to display
//   * @param pageSize Number of computers per page
//   * @param orderBy Computer property used for sorting
//   * @param filter Filter applied on the name column
//   */
//  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Computer, Option[Company])] = {
//
//    //    val offest = pageSize * page
//    //    
//    //    DB.withConnection { implicit connection =>
//    //      
//    //      val computers = SQL(
//    //        """
//    //          select * from computer 
//    //          left join company on computer.company_id = company.id
//    //          where computer.name like {filter}
//    //          order by {orderBy} nulls last
//    //          limit {pageSize} offset {offset}
//    //        """
//    //      ).on(
//    //        'pageSize -> pageSize, 
//    //        'offset -> offest,
//    //        'filter -> filter,
//    //        'orderBy -> orderBy
//    //      ).as(Computer.withCompany *)
//    //
//    //      val totalRows = SQL(
//    //        """
//    //          select count(*) from computer 
//    //          left join company on computer.company_id = company.id
//    //          where computer.name like {filter}
//    //        """
//    //      ).on(
//    //        'filter -> filter
//    //      ).as(scalar[Long].single)
//    //
//    //      Page(computers, page, offest, totalRows)
//
//    null
//  }
//
//  /**
//   * Update a computer.
//   *
//   * @param id The computer id
//   * @param computer The computer values.
//   */
//  def update(id: Long, computer: Computer) = {
//    //    DB.withConnection { implicit connection =>
//    //      SQL(
//    //        """
//    //          update computer
//    //          set name = {name}, introduced = {introduced}, discontinued = {discontinued}, company_id = {company_id}
//    //          where id = {id}
//    //        """
//    //      ).on(
//    //        'id -> id,
//    //        'name -> computer.name,
//    //        'introduced -> computer.introduced,
//    //        'discontinued -> computer.discontinued,
//    //        'company_id -> computer.companyId
//    //      ).executeUpdate()
//    //    }
//  }
//
//  /**
//   * Insert a new computer.
//   *
//   * @param computer The computer values.
//   */
//  def insert(computer: Computer) = {
//    //    DB.withConnection { implicit connection =>
//    //      SQL(
//    //        """
//    //          insert into computer values (
//    //            (select next value for computer_seq), 
//    //            {name}, {introduced}, {discontinued}, {company_id}
//    //          )
//    //        """
//    //      ).on(
//    //        'name -> computer.name,
//    //        'introduced -> computer.introduced,
//    //        'discontinued -> computer.discontinued,
//    //        'company_id -> computer.companyId
//    //      ).executeUpdate()
//    //    }
//  }
//
//  /**
//   * Delete a computer.
//   *
//   * @param id Id of the computer to delete.
//   */
//  def delete(id: Long) = {
//    //    DB.withConnection { implicit connection =>
//    //      SQL("delete from computer where id = {id}").on('id -> id).executeUpdate()
//    //    }
//  }
//
//}
//
//object Company {
//
//  /**
//   * Parse a Company from a ResultSet
//   */
//  val simple = {
//    //    get[Pk[Long]]("company.id") ~
//    //    get[String]("company.name") map {
//    //      case id~name => Company(id, name)
//    //    }
//  }
//
//  /**
//   * Construct the Map[String,String] needed to fill a select options set.
//   */
//  //  def options: Seq[(String,String)] = DB.withConnection { //implicit connection =>
//  //    SQL("select * from company order by name").as(Company.simple *).map(c => c.id.toString -> c.name)
//  //  }
//
//}

