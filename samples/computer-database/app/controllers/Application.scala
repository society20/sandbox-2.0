package controllers

//play core
import play.api._
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

//app
import views._
import models._

//app utils
import models.form.format.FormatsObjectId._
import models.json.ComputerJsonFormat._

//java
import java.text.SimpleDateFormat

//casbah
import com.mongodb.casbah.Imports._

/**
 * Manage a database of computers
 */
object Application extends Controller {

  val dateFormatter = new SimpleDateFormat( "dd MMM yyyy" )
  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect( routes.Application.list( 0, 2, "" ) )

  /**
   * Describe the computer form (used in both edit and create screens).
   */

  //    val objectId = of[Company]
  //    val computerForm = Form(
  //      mapping(
  //        "id" -> ignored( null: ObjectId ),
  //        "name" -> nonEmptyText,
  //        "introduced" -> optional( date( "yyyy-MM-dd" ) ),
  //        "discontinued" -> optional( date( "yyyy-MM-dd" ) ),
  //        "company" -> optional( Company ) )( Computer.apply )( Computer.unapply ) )

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

    val ( size, computers ) = ComputerDAO.jsonListWithCount( page = page, orderBy = orderBy, filter = filter )
    Ok( toJson(
      Map(
        "currentPage" -> toJson( page ),
        "total" -> toJson( size ),
        "computers" -> toJson( computers ) ) ) )
  }

  /**
   * Retrieves a single computer by an id and returns it in Json format.
   */
  def jsonOne( id: String ) = Action {

    ComputerDAO.findById( id ).map {
      computer => Ok( toJson( computer ) )
    }.getOrElse( NotFound )
  }

  /**
   * Display the 'edit form' of a existing Computer.
   *
   * @param id Id of the computer to edit
   */
  def edit( id: String, keepPage: Int = 0 ) = Action {
    //    ComputerDAO.findById( id ).map { computer =>
    //      Ok( html.editForm( id, keepPage, computerForm.fill( computer ) ) )
    //    }.getOrElse( NotFound )
    NotFound
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the computer to edit
   */
  def update( id: String, keepPage: Int = 0 ) = Action { implicit request =>
    //    computerForm.bindFromRequest.fold(
    //      formWithErrors => BadRequest( html.editForm( id, keepPage, formWithErrors ) ),
    //      computer => {
    //        ComputerDAO.save( Computer.compose( id, computer ) )
    //        Redirect( routes.Application.list( keepPage, 2, "" ) ).flashing(
    //          "success" -> "Computer %s has been updated".format( computer.name ),
    //          "updatedId" -> id.toString )
    //      } )
    NotFound
  }

  /**
   * Edits a computer sent in Json format.
   */
  def jsonUpdate( id: String ) = Action( parse.json ) { request =>

    val computerFromClient: Computer = fromJson( request.body )

    //what about validation??

    ComputerDAO.findOneByID( computerFromClient._id ).map {
      foundComputer =>
        val maybeCompany = computerFromClient.company match {
          case Some( cia ) => CompanyDAO.findOneByID( cia._id )
          case _           => None
        }
        val safeComputer = Computer( computerFromClient._id,
          computerFromClient.name,
          computerFromClient.introduced,
          computerFromClient.discontinued,
          maybeCompany )
        ComputerDAO.save( safeComputer )

        //returns the computer being updated
        Ok( toJson( safeComputer ) )
    }.getOrElse( NotFound( "Unable to update the computer because it doesn't exist anymore." ) )
  }

  /**
   * Display the 'new computer form'.
   */
  def create = Action {
    //    Ok( html.createForm( computerForm ) )
    NotFound
  }

  /**
   * Handle the 'new computer form' submission.
   */
  def save = Action { implicit request =>
    //    computerForm.bindFromRequest.fold(
    //      formWithErrors => BadRequest( html.createForm( formWithErrors ) ),
    //      computer => {
    //        ComputerDAO.insert( computer )
    //        Home.flashing( "success" -> "Computer %s has been created".format( computer.name ) )
    //      } )
    NotFound
  }

  /**
   * Handle computer deletion.
   */
  def delete( id: String ) = Action {
    ComputerDAO.removeById( new ObjectId( id ) )
    Home.flashing( "success" -> "Computer has been deleted" )
  }

}
