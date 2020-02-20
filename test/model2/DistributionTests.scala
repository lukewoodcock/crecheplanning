package model2

import java.util.Calendar

import model2.shifts.{ScheduledShift, ShiftDefinition}
import org.scalatest.FunSuite
import play.api.libs.functional.syntax._
import play.api.libs.json._
import services.ShiftManager2
import utils.{CoverUtils, ShiftUtils}

class DistributionTests extends FunSuite {

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


  def testModel(modelString:String):Option[Model] = {
    val testModel: JsValue = Json.parse(modelString)
    val modelValidation = testModel.validate[Model]
    modelValidation.fold(errors => None, model => Some(model))
  }

  def testCoverModel(modelString:String):Option[ScheduleRequirements] = {
    val testModel: JsValue = Json.parse(modelString)
    val modelValidation = testModel.validate[ScheduleRequirements]
    modelValidation.fold(errors => None, model => Some(model))
  }

  ignore("validate model") {
    val jsonModelString = """{"skills":["Guard_Moyens","Gym_Moyens","Organise","Guard_Grands","Gym_Grands"],"shifts":[{"id":"OPEN","category":"ORGANISE","description":"Open crèche","startTime":"08:00:00","endTime":"09:30:00","skillsRequirements":["Organise"]},{"id":"CLOSE","category":"ORGANISE","description":"Close crèche","startTime":"17:00:00","endTime":"18:30:00","skillsRequirements":["Organise"]},{"id":"MORNING_MOYENS","category":"GUARD","description":"Morning guard moyens","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"AFTERNOON_MOYENS","category":"GUARD","description":"Afternoon guard moyens","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"MORNING_GRANDS","category":"GUARD","description":"Morning guard grands","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Grands"]},{"id":"AFTERNOON_GRANDS","category":"GUARD","description":"Afternoon guard grands","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Grands"]},{"id":"GYM_MOYENS","category":"GUARD","description":"Gym Moyens","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Moyens"]},{"id":"GYM_GRANDS","category":"GUARD","description":"Gym Grands","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Grands"]}],"contracts":[{"id":0,"description":"standard_moyens","globalLimits":{"daily":1,"weekly":3},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_MOYENS","AFTERNOON_MOYENS","GYM_MOYENS"],"limits":{"daily":1,"weekly":2}}]},{"id":1,"description":"standard_grands","globalLimits":{"daily":1},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_GRANDS","AFTERNOON_GRANDS","GYM_GRANDS"],"limits":{"daily":1,"weekly":2}}]}],"families":[{"id":"EMMA","contractId":0,"name":"EMMA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LAUTARO","contractId":0,"name":"LAUTARO","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"AIMÉE","contractId":0,"name":"AIMÉE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"JULIETTE","contractId":0,"name":"JULIETTE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUIS","contractId":0,"name":"LOUIS","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"ÉLISA","contractId":0,"name":"ÉLISA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"MARCEAU","contractId":0,"name":"MARCEAU","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUISE","contractId":1,"name":"LOUISE","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"ROMY","contractId":1,"name":"ROMY","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"TIAGO","contractId":1,"name":"TIAGO","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"RUBEN","contractId":1,"name":"RUBEN","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"RAPHAËL","contractId":1,"name":"RAPHAËL","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"SUZANNE","contractId":1,"name":"SUZANNE","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"MARCEAU","contractId":1,"name":"MARCEAU","skills":["Organise","Guard_Grands","Gym_Grands"]}],"coverRequirements":[{"year":2020,"month":1,"weekDefinitions":[{"id":"gym_moyen","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]},{"id":"gym_grands","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_GRANDS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]}]}],"shiftAbsences":[{"shiftDefinitionId":"OPEN","familyId":"EMMA","date":"2020-01-11"}],"dayAbsences":[{"familyId":"MARCEAU","date":"2020-01-11"}]}"""
    assert(testModel(jsonModelString).isDefined)
  }

  ignore("validate cover") {
    val jsonModelString = """{"skills":["Guard_Moyens","Gym_Moyens","Organise","Guard_Grands","Gym_Grands"],"shifts":[{"id":"OPEN","category":"ORGANISE","description":"Open crèche","startTime":"08:00:00","endTime":"09:30:00","skillsRequirements":["Organise"]},{"id":"CLOSE","category":"ORGANISE","description":"Close crèche","startTime":"17:00:00","endTime":"18:30:00","skillsRequirements":["Organise"]},{"id":"MORNING_MOYENS","category":"GUARD","description":"Morning guard moyens","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"AFTERNOON_MOYENS","category":"GUARD","description":"Afternoon guard moyens","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"MORNING_GRANDS","category":"GUARD","description":"Morning guard grands","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Grands"]},{"id":"AFTERNOON_GRANDS","category":"GUARD","description":"Afternoon guard grands","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Grands"]},{"id":"GYM_MOYENS","category":"GUARD","description":"Gym Moyens","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Moyens"]},{"id":"GYM_GRANDS","category":"GUARD","description":"Gym Grands","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Grands"]}],"contracts":[{"id":0,"description":"standard_moyens","globalLimits":{"daily":1,"weekly":3},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_MOYENS","AFTERNOON_MOYENS","GYM_MOYENS"],"limits":{"daily":1,"weekly":2}}]},{"id":1,"description":"standard_grands","globalLimits":{"daily":1},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_GRANDS","AFTERNOON_GRANDS","GYM_GRANDS"],"limits":{"daily":1,"weekly":2}}]}],"families":[{"id":"EMMA","contractId":0,"name":"EMMA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LAUTARO","contractId":0,"name":"LAUTARO","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"AIMÉE","contractId":0,"name":"AIMÉE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"JULIETTE","contractId":0,"name":"JULIETTE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUIS","contractId":0,"name":"LOUIS","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"ÉLISA","contractId":0,"name":"ÉLISA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"MARCEAU","contractId":0,"name":"MARCEAU","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUISE","contractId":1,"name":"LOUISE","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"ROMY","contractId":1,"name":"ROMY","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"TIAGO","contractId":1,"name":"TIAGO","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"RUBEN","contractId":1,"name":"RUBEN","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"RAPHAËL","contractId":1,"name":"RAPHAËL","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"SUZANNE","contractId":1,"name":"SUZANNE","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"MARCEAU","contractId":1,"name":"MARCEAU","skills":["Organise","Guard_Grands","Gym_Grands"]}],"coverRequirements":[{"year":2020,"month":1,"weekDefinitions":[{"id":"gym_moyen","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]},{"id":"gym_grands","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_GRANDS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]}]}],"shiftAbsences":[{"shiftDefinitionId":"OPEN","familyId":"EMMA","date":"2020-01-11"}],"dayAbsences":[{"familyId":"MARCEAU","date":"2020-01-11"}]}"""
    assert(testCoverModel(jsonModelString).isDefined)
  }

  ignore("1 family, 1 day") {
    val testModelString = """{"skills":["Guard_Moyens","Gym_Moyens","Organise","Guard_Grands","Gym_Grands"],"shifts":[{"id":"OPEN","category":"ORGANISE","description":"Open crèche","startTime":"08:00:00","endTime":"09:30:00","skillsRequirements":["Organise"]},{"id":"CLOSE","category":"ORGANISE","description":"Close crèche","startTime":"17:00:00","endTime":"18:30:00","skillsRequirements":["Organise"]},{"id":"MORNING_MOYENS","category":"GUARD","description":"Morning guard moyens","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"AFTERNOON_MOYENS","category":"GUARD","description":"Afternoon guard moyens","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"MORNING_GRANDS","category":"GUARD","description":"Morning guard grands","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Grands"]},{"id":"AFTERNOON_GRANDS","category":"GUARD","description":"Afternoon guard grands","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Grands"]},{"id":"GYM_MOYENS","category":"GUARD","description":"Gym Moyens","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Moyens"]},{"id":"GYM_GRANDS","category":"GUARD","description":"Gym Grands","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Grands"]}],"contracts":[{"id":0,"description":"standard_moyens","globalLimits":{"daily":1,"weekly":3},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_MOYENS","AFTERNOON_MOYENS","GYM_MOYENS"],"limits":{"daily":1,"weekly":2}}]},{"id":1,"description":"standard_grands","globalLimits":{"daily":1},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_GRANDS","AFTERNOON_GRANDS","GYM_GRANDS"],"limits":{"daily":1,"weekly":2}}]}],"families":[{"id":"EMMA","contractId":0,"name":"EMMA","skills":["Organise","Guard_Moyens","Gym_Moyens"]}],"coverRequirements":[{"year":2020,"month":1,"weekDefinitions":[{"id":"gym_moyen","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]},{"id":"gym_grands","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_GRANDS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]}]}],"shiftAbsences":[{"shiftDefinitionId":"OPEN","familyId":"EMMA","date":"2020-01-11"}],"dayAbsences":[{"familyId":"MARCEAU","date":"2020-01-11"}]}"""
    val model = testModel(testModelString)
    assert(model.isDefined)

    val crModel = testCoverModel(testModelString)
    assert(crModel.isDefined)

    val month = CoverUtils.getMonth(crModel.get.coverRequirements.head, model.get.shifts)
    val anchor = month.lift(0).get

    val result = month.lift(0) match {
      case Some(value) => {
        val day = month
          .filter(s => s.date == value.date)
//        day.foreach(s => {
//          println("shiftsToResolve : " + s)
//        })
        ShiftManager2.resolve(model.get.families, ShiftUtils.groupShiftsByWeek(day), model.get.contracts)
      }
      case None => {
//        println("something went wrong")
        List.empty[ScheduledShift]
      }
    }

    assert(result.size == 1)
  }

  ignore("5 families, 5 days, 3 shift") {

    val testModelString = """{"skills":["Guard_Moyens","Gym_Moyens","Organise","Guard_Grands","Gym_Grands"],"shifts":[{"id":"OPEN","category":"ORGANISE","description":"Open crèche","startTime":"08:00:00","endTime":"09:30:00","skillsRequirements":["Organise"]},{"id":"MORNING_MOYENS","category":"GUARD","description":"Morning guard moyens","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"AFTERNOON_MOYENS","category":"GUARD","description":"Afternoon guard moyens","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"MORNING_GRANDS","category":"GUARD","description":"Morning guard grands","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Grands"]},{"id":"AFTERNOON_GRANDS","category":"GUARD","description":"Afternoon guard grands","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Grands"]},{"id":"GYM_MOYENS","category":"GUARD","description":"Gym Moyens","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Moyens"]},{"id":"GYM_GRANDS","category":"GUARD","description":"Gym Grands","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Grands"]}],"contracts":[{"id":0,"description":"standard_moyens","globalLimits":{"daily":1,"weekly":3},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_MOYENS","AFTERNOON_MOYENS","GYM_MOYENS"],"limits":{"daily":1,"weekly":2}}]},{"id":1,"description":"standard_grands","globalLimits":{"daily":1},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_GRANDS","AFTERNOON_GRANDS","GYM_GRANDS"],"limits":{"daily":1,"weekly":2}}]}],"families":[{"id":"EMMA","contractId":0,"name":"EMMA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LAUTARO","contractId":0,"name":"LAUTARO","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"AIMÉE","contractId":0,"name":"AIMÉE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"JULIETTE","contractId":0,"name":"JULIETTE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUIS","contractId":0,"name":"LOUIS","skills":["Organise","Guard_Moyens","Gym_Moyens"]}],"coverRequirements":[{"year":2020,"month":1,"weekDefinitions":[{"id":"gym_moyen","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1}]}]}]}],"shiftAbsences":[{"shiftDefinitionId":"OPEN","familyId":"EMMA","date":"2020-01-11"}],"dayAbsences":[{"familyId":"MARCEAU","date":"2020-01-11"}]}"""
    val model = testModel(testModelString)
    assert(model.isDefined)

    val crModel = testCoverModel(testModelString)
    assert(crModel.isDefined)

    val month = CoverUtils.getMonth(crModel.get.coverRequirements.head, model.get.shifts)
    val week = month.filter(s => s.date.get(Calendar.WEEK_OF_YEAR) == 2)
//    week.foreach(s => {
//        println("shiftsToResolve : " + s)
//      })

    val result = ShiftManager2.resolve(model.get.families, ShiftUtils.groupShiftsByWeek(week), model.get.contracts)
        .groupBy(_._2)

    assert(result.keys.flatten.toList
      .filter(f => f.shifts.size != 3)
        .filter(f => f.shifts.filter(s => s.definition.category.equalsIgnoreCase("guard")).size != 2)
        .filter(f => f.shifts.filter(s => s.definition.category.equalsIgnoreCase("ORGANISE")).size != 2)
        .isEmpty
    )
  }

  ignore("november 2019") {
    val testModelString = """{"skills":["Guard_Moyens","Gym_Moyens","Organise","Guard_Grands","Gym_Grands"],"shifts":[{"id":"OPEN","category":"ORGANISE","description":"Open crèche","startTime":"08:00:00","endTime":"09:30:00","skillsRequirements":["Organise"]},{"id":"CLOSE","category":"ORGANISE","description":"Close crèche","startTime":"17:00:00","endTime":"18:30:00","skillsRequirements":["Organise"]},{"id":"MORNING_MOYENS","category":"GUARD","description":"Morning guard moyens","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"AFTERNOON_MOYENS","category":"GUARD","description":"Afternoon guard moyens","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"MORNING_GRANDS","category":"GUARD","description":"Morning guard grands","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Grands"]},{"id":"AFTERNOON_GRANDS","category":"GUARD","description":"Afternoon guard grands","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Grands"]},{"id":"GYM_MOYENS","category":"GUARD","description":"Gym Moyens","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Moyens"]},{"id":"GYM_GRANDS","category":"GUARD","description":"Gym Grands","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Grands"]}],"contracts":[{"id":0,"description":"standard_moyens","globalLimits":{"daily":1,"weekly":3},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_MOYENS","AFTERNOON_MOYENS","GYM_MOYENS"],"limits":{"daily":1,"weekly":2}}]},{"id":1,"description":"standard_grands","globalLimits":{"daily":1},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_GRANDS","AFTERNOON_GRANDS","GYM_GRANDS"],"limits":{"daily":1,"weekly":2}}]}],"families":[{"id":"EMMA","contractId":0,"name":"EMMA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LAUTARO","contractId":0,"name":"LAUTARO","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"AIMÉE","contractId":0,"name":"AIMÉE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"JULIETTE","contractId":0,"name":"JULIETTE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUIS","contractId":0,"name":"LOUIS","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"ÉLISA","contractId":0,"name":"ÉLISA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"MARCEAU","contractId":0,"name":"MARCEAU","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUISE","contractId":1,"name":"LOUISE","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"ROMY","contractId":1,"name":"ROMY","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"TIAGO","contractId":1,"name":"TIAGO","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"RUBEN","contractId":1,"name":"RUBEN","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"RAPHAËL","contractId":1,"name":"RAPHAËL","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"SUZANNE","contractId":1,"name":"SUZANNE","skills":["Organise","Guard_Grands","Gym_Grands"]}],"coverRequirements":[{"year":2019,"month":11,"weekDefinitions":[{"id":"gym_moyen","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]},{"id":"gym_grands","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_GRANDS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]}]}]}"""
    val model = testModel(testModelString)
    assert(model.isDefined)

    val crModel = testCoverModel(testModelString)
    assert(crModel.isDefined)

    val month = CoverUtils.getMonth(crModel.get.coverRequirements.head, model.get.shifts)

    val result = ShiftManager2.resolve(model.get.families, ShiftUtils.groupShiftsByWeek(month), model.get.contracts)
      .groupBy(_._2)

    model.get.families.foreach(f => {

      // check only one shift per day
      assert(f.shifts.groupBy(_.date).forall(_._2.size < 2))

      println("\n\n=========\n" + f.id)
      ShiftUtils.groupShiftsByWeek(f.shifts.toArray).foreach(w => {
        println("===")
        w._2.foreach(s => println(s))
      })

    result.foreach(week => {
        val byWeek = ShiftUtils.groupShiftsByWeek(f.shifts.toArray)
        assert(byWeek.map(s => s._2.size).forall(_ <= 3))
        byWeek.foreach(w => {
          assert(w._2.find(s => s.definition.category.equalsIgnoreCase("guard")).size < 3)
          assert(w._2.find(s => s.definition.category.equalsIgnoreCase("organise")).size < 2)
        })
      })
    })

    assert(month.forall(s => s.family.isDefined))
  }


  test("november 2019 with absence") {
    val testModelString = """{"skills":["Guard_Moyens","Gym_Moyens","Organise","Guard_Grands","Gym_Grands"],"shifts":[{"id":"OPEN","category":"ORGANISE","description":"Open crèche","startTime":"08:00:00","endTime":"09:30:00","skillsRequirements":["Organise"]},{"id":"CLOSE","category":"ORGANISE","description":"Close crèche","startTime":"17:00:00","endTime":"18:30:00","skillsRequirements":["Organise"]},{"id":"MORNING_MOYENS","category":"GUARD","description":"Morning guard moyens","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"AFTERNOON_MOYENS","category":"GUARD","description":"Afternoon guard moyens","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"MORNING_GRANDS","category":"GUARD","description":"Morning guard grands","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Grands"]},{"id":"AFTERNOON_GRANDS","category":"GUARD","description":"Afternoon guard grands","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Grands"]},{"id":"GYM_MOYENS","category":"GUARD","description":"Gym Moyens","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Moyens"]},{"id":"GYM_GRANDS","category":"GUARD","description":"Gym Grands","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Grands"]}],"contracts":[{"id":0,"description":"standard_moyens","globalLimits":{"daily":1,"weekly":3},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_MOYENS","AFTERNOON_MOYENS","GYM_MOYENS"],"limits":{"daily":1,"weekly":2}}]},{"id":1,"description":"standard_grands","globalLimits":{"daily":1},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_GRANDS","AFTERNOON_GRANDS","GYM_GRANDS"],"limits":{"daily":1,"weekly":2}}]}],"families":[{"id":"EMMA","contractId":0,"name":"EMMA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LAUTARO","contractId":0,"name":"LAUTARO","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"AIMÉE","contractId":0,"name":"AIMÉE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"JULIETTE","contractId":0,"name":"JULIETTE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUIS","contractId":0,"name":"LOUIS","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"ÉLISA","contractId":0,"name":"ÉLISA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"MARCEAU","contractId":0,"name":"MARCEAU","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUISE","contractId":1,"name":"LOUISE","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"ROMY","contractId":1,"name":"ROMY","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"TIAGO","contractId":1,"name":"TIAGO","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"RUBEN","contractId":1,"name":"RUBEN","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"RAPHAËL","contractId":1,"name":"RAPHAËL","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"SUZANNE","contractId":1,"name":"SUZANNE","skills":["Organise","Guard_Grands","Gym_Grands"]}],"coverRequirements":[{"year":2019,"month":11,"weekDefinitions":[{"id":"gym_moyen","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]},{"id":"gym_grands","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_GRANDS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]}]}],"shiftAbsences":[{"shiftDefinitionId":"OPEN","familyId":"EMMA","date":"2019-11-01"}],"dateAbsences":[{"familyId":"MARCEAU","date":"2019-11-04"}]}"""
    val model = testModel(testModelString)
    assert(model.isDefined)

    val crModel = testCoverModel(testModelString)
    assert(crModel.isDefined)

    val month = CoverUtils.getMonth(crModel.get.coverRequirements.head, model.get.shifts)

    if(crModel.get.shiftAbsences.isDefined) {
      Family.addAbsences(model.get.families, crModel.get.shiftAbsences.get)
    }

    if(crModel.get.dateAbsences.isDefined) {
      Family.addAbsences(model.get.families, crModel.get.dateAbsences.get)
    }

    val result = ShiftManager2.resolve(model.get.families, ShiftUtils.groupShiftsByWeek(month), model.get.contracts)
      .groupBy(_._2)

    model.get.families.foreach(f => {

      // check only one shift per day
      assert(f.shifts.groupBy(_.date).forall(_._2.size < 2))

      println("\n\n=========\n" + f.id)
      ShiftUtils.groupShiftsByWeek(f.shifts.toArray).foreach(w => {
        println("===")
        w._2.foreach(s => println(s))
      })

      result.foreach(week => {
        val byWeek = ShiftUtils.groupShiftsByWeek(f.shifts.toArray)
        assert(byWeek.map(s => s._2.size).forall(_ <= 3))
        byWeek.foreach(w => {
          assert(w._2.find(s => s.definition.category.equalsIgnoreCase("guard")).size < 3)
          assert(w._2.find(s => s.definition.category.equalsIgnoreCase("organise")).size < 2)
        })
      })
    })

    assert(month.forall(s => s.family.isDefined))
    assert(!(month.head.family.get.id == "EMMA"))

    val cal = Calendar.getInstance()
    cal.set(2019, 11, 4)

    assert(month.filter(s => s.date == cal).filter(s => s.family.get.id == "MARCEAU").isEmpty)
  }
}
