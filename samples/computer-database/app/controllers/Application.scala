package controllers

import play.api._
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import anorm._
import views._
import models._
import com.mongodb.casbah.Imports._
import models.form.format.FormatsObjectId._
import models.json.ComputerJsonFormat
import java.text.SimpleDateFormat

/**
 * Manage a database of computers
 */
object Application extends Controller {

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect( routes.Application.list( 0, 2, "" ) )

  /**
   * Describe the computer form (used in both edit and create screens).
   */

  val objectId = of[ObjectId]

  val computerForm = Form(
    mapping(
      "id" -> ignored( null: ObjectId ),
      "name" -> nonEmptyText,
      "introduced" -> optional( date( "yyyy-MM-dd" ) ),
      "discontinued" -> optional( date( "yyyy-MM-dd" ) ),
      "company" -> optional( objectId ) )( Computer.apply )( Computer.unapply ) )

  // -- Actions

  /**
   * Handle default path requests, redirect to computers list
   */
  def index = Action { Home }

  /**
   * Display the paginated list of computers.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on computer names
   */
  def list( page: Int, orderBy: Int, filter: String ) = Action { implicit request =>
    Ok( html.list(
      ComputerDAO.list( page = page, orderBy = orderBy, filter = filter ),
      orderBy, filter ) )
  }
  def jsonList( page: Int, orderBy: Int, filter: String ) = Action {
    val requestResults = ComputerDAO.jsonListWithCount( page = page, orderBy = orderBy, filter = filter )
    val resultsCount = requestResults._1
    val computerCompanyTuples = requestResults._2
    val dateFromatter = new SimpleDateFormat( "dd-M-yyyy" )
    Ok( toJson( JsObject( List(
      "total" -> JsNumber( resultsCount ),
      "currentPage" -> JsNumber( page ),
      "computers" -> JsArray(
        computerCompanyTuples.map( tuple => JsObject( List(
          "_id" -> JsString( tuple._1._id.toString ),
          "name" -> JsString( tuple._1.name ),
          "introduced" -> JsString( tuple._1.introduced.map( date => dateFromatter.format( date ) ).getOrElse( "" ) ),
          "discontinued" -> JsString( tuple._1.discontinued.map( date => dateFromatter.format( date ) ) getOrElse ( "" ) ),
          "company" -> JsString( tuple._2.map( company => company.name ).getOrElse( "" ) ) ) ) ) ) ) ) ) )
  }

  /**
   * Display the 'edit form' of a existing Computer.
   *
   * @param id Id of the computer to edit
   */
  def edit( id: String, keepPage: Int = 0 ) = Action {
    ComputerDAO.findById( id ).map { computer =>
      Ok( html.editForm( id, keepPage, computerForm.fill( computer ) ) )
    }.getOrElse( NotFound )
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the computer to edit
   */
  def update( id: String, keepPage: Int = 0 ) = Action { implicit request =>
    computerForm.bindFromRequest.fold(
      formWithErrors => BadRequest( html.editForm( id, keepPage, formWithErrors ) ),
      computer => {
        ComputerDAO.save( Computer.compose( id, computer ) )
        Redirect( routes.Application.list( keepPage, 2, "" ) ).flashing(
          "success" -> "Computer %s has been updated".format( computer.name ),
          "updatedId" -> id.toString )
      } )
  }

  /**
   * Display the 'new computer form'.
   */
  def create = Action {
    Ok( html.createForm( computerForm ) )
  }

  /**
   * Handle the 'new computer form' submission.
   */
  def save = Action { implicit request =>
    computerForm.bindFromRequest.fold(
      formWithErrors => BadRequest( html.createForm( formWithErrors ) ),
      computer => {
        ComputerDAO.insert( computer )
        Home.flashing( "success" -> "Computer %s has been created".format( computer.name ) )
      } )
  }

  /**
   * Handle computer deletion.
   */
  def delete( id: String ) = Action {
    ComputerDAO.removeById( new ObjectId( id ) )
    Home.flashing( "success" -> "Computer has been deleted" )
  }

}
