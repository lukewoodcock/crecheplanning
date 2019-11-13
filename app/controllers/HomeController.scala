package controllers

import play.api.mvc._
import javax.inject._
import data.DataManager
import model.{Family, Shift, ShiftType}
import services.ShiftManager

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  val dm = new DataManager()
  val shifts: List[Shift] = dm.mockWeek()
  val families: List[Family] = dm.mockFamilies()

  val result: List[(Shift, Option[Family])] = ShiftManager.autoFill(shifts, families, Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1)))
  for(s <- result) {
    println("Shift:" + s)
  }

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index(result.toString()))
  }

}
