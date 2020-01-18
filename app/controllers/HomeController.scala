package controllers

import java.time.Month

import javax.inject._
import model2._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsPath, Json, Reads}
import play.api.mvc._
import utils.DateUtils

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Hello world"))
  }


  implicit val shiftsReads: Reads[ShiftDefinition] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "startTime").read[String] and
      (JsPath \ "endTime").read[String] and
      (JsPath \ "skillsRequirements").read[List[String]]
    )(ShiftDefinition.apply _)

  implicit val limitsReads: Reads[Limits] = (
    (JsPath \ "daily").readNullable[Int] and
      (JsPath \ "weekly").readNullable[Int] and
      (JsPath \ "monthly").readNullable[Int]
    )(Limits.apply _)

  implicit val shiftRuleReads: Reads[ShiftRule] = (
    (JsPath \ "shiftTypes").read[List[String]] and
      (JsPath \ "limits").read[Limits]
    )(ShiftRule.apply _)

  implicit val contractsReads: Reads[Contract] = (
    (JsPath \ "id").read[Int] and
      (JsPath \ "shiftRules").read[List[ShiftRule]]
  )(Contract.apply _)

  implicit val familyReads: Reads[Family] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "contractId").read[Int] and
      (JsPath \ "name").read[String] and
      (JsPath \ "skills").read[List[String]]
    )(Family.apply _)

  implicit val modelReads: Reads[Model] = (
    (JsPath \ "skills").read[List[String]] and
      (JsPath \ "shifts").read[List[ShiftDefinition]] and
      (JsPath \ "contracts").read[List[Contract]] and
      (JsPath \ "families").read[List[Family]]
    )(Model.apply _)

  def modelPost = Action(parse.json) { request =>
    val requestValidation = request.body.validate[Model]
    requestValidation.fold(
      errors => {
        BadRequest(Json.obj("message" -> JsError.toJson(errors)))
      },
      model => {
        Ok(views.html.index("Hello world".concat(request.body.toString())))
      }
    )
  }

  implicit val coverDefinitionReads: Reads[Cover] = (
    (JsPath \ "shiftType").read[String] and
      (JsPath \ "cover").read[Int]
    )(Cover.apply _)

  implicit val dayOfweekDefinitionReads: Reads[Day] = (
    (JsPath \ "day").read[Int] and
      (JsPath \ "shifts").read[List[Cover]]
    )(Day.apply _)

  implicit val weekDefinitionReads: Reads[WeekDefinition] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "days").read[List[Day]]
    )(WeekDefinition.apply _)

  implicit val coverRequirementsReads: Reads[CoverRequirements] = (
    (JsPath \ "year").read[Int] and
      (JsPath \ "month").read[Int] and
      (JsPath \ "weekDefinitions").read[List[WeekDefinition]]
  )(CoverRequirements.apply _)

  implicit val absenceReads: Reads[Absence] = (
    (JsPath \ "date").read[String] and
      (JsPath \ "familyId").read[String] and
      (JsPath \ "shiftId").readNullable[String]
    )(Absence.apply _)

  implicit val scheduleRequirementsReads: Reads[ScheduleRequirements] = (
    (JsPath \ "coverRequirements").read[List[CoverRequirements]] and
      (JsPath \ "shiftAbsences").readNullable[List[Absence]] and
      (JsPath \ "dateAbsences").readNullable[List[Absence]]
    )(ScheduleRequirements.apply _)

  def coverPost = Action(parse.json) { request =>
    val requestValidation = request.body.validate[ScheduleRequirements]
    requestValidation.fold(
      errors => {
        BadRequest(Json.obj("message" -> JsError.toJson(errors)))
      },
      model => {
        Ok(views.html.index("Hello world".concat(request.body.toString())))
      }
    )
  }

  def month(month: String) = Action {
    val days = DateUtils.getMonth(month.toLowerCase match {
      case "january" => Month.JANUARY
      case "february" => Month.FEBRUARY
      case "march" => Month.MARCH
      case "april" => Month.APRIL
      case "may" => Month.MAY
      case "june" => Month.JUNE
      case "july" => Month.JULY
      case "august" => Month.AUGUST
      case "september" => Month.SEPTEMBER
      case "october" => Month.OCTOBER
      case "november" => Month.NOVEMBER
      case "december" => Month.DECEMBER
      case _ => Month.JANUARY
    })
    Ok(views.html.month("I am month ".concat(month), days.toList.toString()))
  }

}
