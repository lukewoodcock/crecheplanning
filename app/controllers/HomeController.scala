package controllers

import javax.inject._
import model._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import services.ShiftManager
import utils.{CoverUtils, ShiftUtils}

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
    Ok("TODO - read me\nPost json model to ./shifts")
  }

  implicit val shiftsReads: Reads[ShiftDefinition] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "category").read[String] and
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
    (JsPath \ "shiftDefinitionIds").read[List[String]] and
      (JsPath \ "limits").read[Limits]
    )(ShiftRule.apply _)

  implicit val contractsReads: Reads[Contract] = (
    (JsPath \ "id").read[Int] and
      (JsPath \ "globalLimits").read[Limits] and
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

  ////////////



  implicit val coverDefinitionReads: Reads[Cover] = (
    (JsPath \ "shiftDefinitionId").read[String] and
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
      (JsPath \ "shiftDefinitionId").readNullable[String]
    )(Absence.apply _)

  implicit val scheduleRequirementsReads: Reads[ScheduleRequirements] = (
    (JsPath \ "shifts").read[List[ShiftDefinition]] and
      (JsPath \ "coverRequirements").read[List[CoverRequirements]] and
      (JsPath \ "shiftAbsences").readNullable[List[Absence]] and
      (JsPath \ "dateAbsences").readNullable[List[Absence]]
    )(ScheduleRequirements.apply _)

  implicit val ScheduledShiftResultWrites: Writes[ScheduledShiftResult] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "start").write[String] and
      (JsPath \ "end").write[String] and
      (JsPath \ "family").write[String]
  )(unlift(ScheduledShiftResult.unapply))

  def modelPost = Action(parse.json) { request =>
    val modelValidation = request.body.validate[Model]
    modelValidation.fold(
      errors => {
        BadRequest(Json.obj("message" -> JsError.toJson(errors)))
      },
      model => {
        //check family contract ids are defined in contracts
        val contracts = model.contracts.groupBy(_.id)
        val cond = model.families.map(f => contracts.get(f.contractId))
        if(cond.contains(None)) {
          BadRequest("Families contains Family with undefined contract")
        } else {

          val requestValidation = request.body.validate[ScheduleRequirements]
          requestValidation.fold(
            errors => {
              BadRequest(Json.obj("message" -> JsError.toJson(errors)))
            },
            crModel => {

              if(crModel.coverRequirements.size > 1) {
                BadRequest("API currently only handles 1 month at a time")
              } else if (crModel.coverRequirements.size < 1) {
                BadRequest("Missing cover requirements")
              } else {

                val month = CoverUtils.getMonth(crModel.coverRequirements.head, model.shifts)
                if(crModel.shiftAbsences.isDefined) {
                  Family.addAbsences(model.families, crModel.shiftAbsences.get)
                }

                if(crModel.dateAbsences.isDefined) {
                  Family.addAbsences(model.families, crModel.dateAbsences.get)
                }

                val shifts = ShiftManager.resolve(model.families, ShiftUtils.groupShiftsByWeek(month), model.contracts)
                val result = shifts
                  .groupBy(_._2)

                val json = Json.toJson(month.map(s => ScheduledShiftResult.fromScheduledShift(s)))

                Ok(json.toString())
              }
            }
          )
        }
      }
    )
  }
}
