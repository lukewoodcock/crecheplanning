package model2

import java.time.Month
import java.util.Calendar

import play.api.libs.functional.syntax._
import model.{Family, Shift, ShiftType}
import model2.shifts.{ScheduledShift, ShiftDefinition}
import org.joda.time.DateTime
import org.scalatest.FunSuite
import play.api.libs.json._
import services.ShiftManager
import utils.{DateUtils, TestUtils}
import utils.DateUtils.getMonth

import scala.collection.mutable.ListBuffer
import scala.util.Random
import scala.util.control.Breaks.{break, breakable}

class DistributionTests extends FunSuite {

//  val jsonModelString = """{"skills":["Guard_Moyens","Gym_Moyens","Organise","Guard_Grands","Gym_Grands"],"shifts":[{"id":"OPEN","category":"ORGANISE","description":"Open crèche","startTime":"08:00:00","endTime":"09:30:00","skillsRequirements":["Organise"]},{"id":"CLOSE","category":"ORGANISE","description":"Close crèche","startTime":"17:00:00","endTime":"18:30:00","skillsRequirements":["Organise"]},{"id":"MORNING_MOYENS","category":"GUARD","description":"Morning guard moyens","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"AFTERNOON_MOYENS","category":"GUARD","description":"Afternoon guard moyens","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Moyens"]},{"id":"MORNING_GRANDS","category":"GUARD","description":"Morning guard grands","startTime":"09:30:00","endTime":"13:30:00","skillsRequirements":["Guard_Grands"]},{"id":"AFTERNOON_GRANDS","category":"GUARD","description":"Afternoon guard grands","startTime":"13:30:00","endTime":"18:30:00","skillsRequirements":["Guard_Grands"]},{"id":"GYM_MOYENS","category":"GUARD","description":"Gym Moyens","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Moyens"]},{"id":"GYM_GRANDS","category":"GUARD","description":"Gym Grands","startTime":"09:30:00","endTime":"11:30:00","skillsRequirements":["Gym_Grands"]}],"contracts":[{"id":0,"description":"standard_moyens","globalLimits":{"daily":1,"weekly":3},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_MOYENS","AFTERNOON_MOYENS","GYM_MOYENS"],"limits":{"daily":1,"weekly":2}}]},{"id":1,"description":"standard_grands","globalLimits":{"daily":1},"shiftRules":[{"shiftDefinitionIds":["OPEN","CLOSE"],"limits":{"daily":1,"weekly":1}},{"shiftDefinitionIds":["MORNING_GRANDS","AFTERNOON_GRANDS","GYM_GRANDS"],"limits":{"daily":1,"weekly":2}}]}],"families":[{"id":"EMMA","contractId":0,"name":"EMMA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LAUTARO","contractId":0,"name":"LAUTARO","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"AIMÉE","contractId":0,"name":"AIMÉE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"JULIETTE","contractId":0,"name":"JULIETTE","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUIS","contractId":0,"name":"LOUIS","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"ÉLISA","contractId":0,"name":"ÉLISA","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"MARCEAU","contractId":0,"name":"MARCEAU","skills":["Organise","Guard_Moyens","Gym_Moyens"]},{"id":"LOUISE","contractId":1,"name":"LOUISE","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"ROMY","contractId":1,"name":"ROMY","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"TIAGO","contractId":1,"name":"TIAGO","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"RUBEN","contractId":1,"name":"RUBEN","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"RAPHAËL","contractId":1,"name":"RAPHAËL","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"SUZANNE","contractId":1,"name":"SUZANNE","skills":["Organise","Guard_Grands","Gym_Grands"]},{"id":"MARCEAU","contractId":1,"name":"MARCEAU","skills":["Organise","Guard_Grands","Gym_Grands"]}],"coverRequirements":[{"year":2020,"month":1,"weekDefinitions":[{"id":"gym_moyen","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]},{"id":"gym_grands","days":[{"day":2,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":3,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":4,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":5,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"GYM_GRANDS","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]},{"day":6,"shifts":[{"shiftDefinitionId":"OPEN","cover":1},{"shiftDefinitionId":"MORNING_MOYENS","cover":1},{"shiftDefinitionId":"MORNING_GRANDS","cover":1},{"shiftDefinitionId":"AFTERNOON_MOYENS","cover":1},{"shiftDefinitionId":"AFTERNOON_GRANDS","cover":1},{"shiftDefinitionId":"CLOSE","cover":1}]}]}]}],"shiftAbsences":[{"shiftDefinitionId":"OPEN","familyId":"EMMA","date":"2020-01-11"}],"dayAbsences":[{"familyId":"MARCEAU","date":"2020-01-11"}]}"""
//  val jsonModel: JsValue = Json.parse(jsonModelString)
//
//  implicit val shiftsReads: Reads[ShiftDefinition] = (
//    (JsPath \ "id").read[String] and
//      (JsPath \ "category").read[String] and
//      (JsPath \ "description").read[String] and
//      (JsPath \ "startTime").read[String] and
//      (JsPath \ "endTime").read[String] and
//      (JsPath \ "skillsRequirements").read[List[String]]
//    )(ShiftDefinition.apply _)
//
//  implicit val limitsReads: Reads[Limits] = (
//    (JsPath \ "daily").readNullable[Int] and
//      (JsPath \ "weekly").readNullable[Int] and
//      (JsPath \ "monthly").readNullable[Int]
//    )(Limits.apply _)
//
//  implicit val shiftRuleReads: Reads[ShiftRule] = (
//    (JsPath \ "shiftDefinitionIds").read[List[String]] and
//      (JsPath \ "limits").read[Limits]
//    )(ShiftRule.apply _)
//
//  implicit val contractsReads: Reads[Contract] = (
//    (JsPath \ "id").read[Int] and
//      (JsPath \ "globalLimits").read[Limits] and
//      (JsPath \ "shiftRules").read[List[ShiftRule]]
//    )(Contract.apply _)
//
//  implicit val familyReads: Reads[Family] = (
//    (JsPath \ "id").read[String] and
//      (JsPath \ "contractId").read[Int] and
//      (JsPath \ "name").read[String] and
//      (JsPath \ "skills").read[List[String]]
//    )(Family.apply _)
//
//  implicit val modelReads: Reads[Model] = (
//    (JsPath \ "skills").read[List[String]] and
//      (JsPath \ "shifts").read[List[ShiftDefinition]] and
//      (JsPath \ "contracts").read[List[Contract]] and
//      (JsPath \ "families").read[List[Family]]
//    )(Model.apply _)
//
//  implicit val coverDefinitionReads: Reads[Cover] = (
//    (JsPath \ "shiftDefinitionId").read[String] and
//      (JsPath \ "cover").read[Int]
//    )(Cover.apply _)
//
//  implicit val dayOfweekDefinitionReads: Reads[Day] = (
//    (JsPath \ "day").read[Int] and
//      (JsPath \ "shifts").read[List[Cover]]
//    )(Day.apply _)
//
//  implicit val weekDefinitionReads: Reads[WeekDefinition] = (
//    (JsPath \ "id").read[String] and
//      (JsPath \ "days").read[List[Day]]
//    )(WeekDefinition.apply _)
//
//  implicit val coverRequirementsReads: Reads[CoverRequirements] = (
//    (JsPath \ "year").read[Int] and
//      (JsPath \ "month").read[Int] and
//      (JsPath \ "weekDefinitions").read[List[WeekDefinition]]
//    )(CoverRequirements.apply _)
//
//  implicit val absenceReads: Reads[Absence] = (
//    (JsPath \ "date").read[String] and
//      (JsPath \ "familyId").read[String] and
//      (JsPath \ "shiftDefinitionId").readNullable[String]
//    )(Absence.apply _)
//
//  implicit val scheduleRequirementsReads: Reads[ScheduleRequirements] = (
//    (JsPath \ "shifts").read[List[ShiftDefinition]] and
//    (JsPath \ "coverRequirements").read[List[CoverRequirements]] and
//      (JsPath \ "shiftAbsences").readNullable[List[Absence]] and
//      (JsPath \ "dateAbsences").readNullable[List[Absence]]
//    )(ScheduleRequirements.apply _)
//
//
//  val modelValidation = jsonModel.validate[Model]
//
//  def testModel():Option[Model] = modelValidation.fold(errors => None, model => Some(model))
//
//  test("validate model") {
//    assert(testModel().isDefined)
//  }
//
//
//  val coverValidation = jsonModel.validate[ScheduleRequirements]
//  def coverModel():Option[ScheduleRequirements] = coverValidation.fold(errors => None, model => Some(model))
//
//  test("validate cover") {
//    assert(coverModel().isDefined)
//  }
//
//  def mockMonth(month:Month) = {
//    val m = DateUtils.getMonth(Month.NOVEMBER)
//    val out = ListBuffer[List[Shift]]()
//    for(d <- m) {
//      out += TestUtils.mockDay(d)
//    }
//    out.toList.flatten
//  }
//
//  def mockWeek(month:Month):List[Shift] = {
//    val m = DateUtils.getMonth(Month.NOVEMBER)
//    val w1 = m.map(c => c.get(Calendar.WEEK_OF_YEAR))
//    val out = ListBuffer[List[Shift]]()
//    for(d <- m) {
//      out += TestUtils.mockDay(d)
//    }
//    out.toList.flatten
//  }
//
//  /**
//    * ==== Moyens
//    * EMMA
//    * LAUTARO
//    * AIMÉE
//    * JULIETTE
//    * LOUIS
//    * ÉLISA
//    * MARCEAU
//    *
//    * ==== Grands
//    * LOUISE
//    * ROMY
//    * TIAGO
//    * RUBEN
//    * RAPHAËL
//    * SUZANNE
//    */
//  def mockFamilies(names:List[String]): List[Family] = testModel.get.families
//
////  coverModel.get.coverRequirements.head.
//
//  def moyenDay(date:Calendar) = List[Shift](
//    Shift(Shift.NAMES.MORNING_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), date, None),
//    Shift(Shift.NAMES.AFTERNOON_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), date, None)
//  )
//
//  def grandDay(date:Calendar) = List[Shift](
//    Shift(Shift.NAMES.MORNING_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 4.0), date, None),
//    Shift(Shift.NAMES.AFTERNOON_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_GRANDS, Shift.TYPES.GUARD, 4.0), date, None)
//  )
//
//  def organiseDay(date:Calendar) = List[Shift](
//    Shift(Shift.NAMES.OPENING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), date, None),
//    Shift(Shift.NAMES.CLOSING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), date, None)
//  )
//
//  def getWeeksInMonth(month:Month) = getMonth(month).groupBy(_.get(Calendar.WEEK_OF_YEAR))
//
//  def getShiftsInMonth(month:Month, shiftDefinition:(Calendar) => List[Shift]) = {
//    val weeks = getWeeksInMonth(month)
//    weeks.map(w => {
//      (w._1 -> {
//        w._2.map(d => shiftDefinition(d)).toList.flatten
//      })
//    })
//  }
//
//  def getShiftsForAWeek(month:Month, shiftDefinition:(Calendar) => List[Shift]) = {
//
//    val w = getWeeksInMonth(month)
//      .filter(i => i._2.size == 5)
//    val weeks = getShiftsInMonth(month, shiftDefinition)
//    weeks
//      .filter(i => i._1 == w.head._1)
//  }
//
//  test("1 family, 1 day") {
//    var fMoyens = mockFamilies(List("EMMA"))
//    val firstWeekM = getShiftsInMonth(Month.NOVEMBER, moyenDay).toList.sortBy(_._1).head
//    val mShiftsToResolve = Map(firstWeekM._1 -> firstWeekM._2.filter(s => s.date == firstWeekM._2.head.date)).toList
//    val firstWeekO = getShiftsInMonth(Month.NOVEMBER, organiseDay).toList.sortBy(_._1).head
//    val orgShiftsToResolve = Map(firstWeekO._1 -> firstWeekO._2.filter(s => s.date == firstWeekM._2.head.date)).toList
//
//
//    //    println("mShiftsToResolve = ", mShiftsToResolve.map(s => s._2.map(i => i.toString())))
//    println("mShiftsToResolve = ", mShiftsToResolve)
//
//    val limits = Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
//    val moyenResult = ShiftManager.resolve(fMoyens, mShiftsToResolve, limits)
//    //    println("moyenResult: " + moyenResult)
//
//    val orgResult = ShiftManager.resolve(moyenResult, orgShiftsToResolve, limits)
//    //    println("orgResult: " + orgResult)
//
//
//    println("RESULTS:")
//    moyenResult.foreach(f => {
//      println("\nallResult: ".concat(f.id.concat(" family total: ").concat(f.shifts.size.toString)))
//      f.shifts.foreach(s => {
//        println(s.shiftType.id.concat(" : ").concat(s.date.getTime.toString))
//      })
//    })
//
//    (mShiftsToResolve ::: orgShiftsToResolve).toMap.values.flatten.foreach(s => println(s))
//    assert(fMoyens.find(f => f.id == "EMMA").get.shifts.size == 1)
//  }
//
//  /**
//    * Gabriel has 3 List(OPENING2, AFTERNOON_MOYENS3, MORNING_GRANDS4)
//    * Elisa has 3 List(AFTERNOON_MOYENS1, OPENING4, MORNING_GRANDS5)
//    * Emma has 3 List(MORNING_GRANDS1, OPENING3, AFTERNOON_MOYENS4)
//    * Florentin has 2 List(MORNING_GRANDS2, OPENING5)
//    * No family AFTERNOON_MOYENS5
//    * Lautaro has 3 List(OPENING1, AFTERNOON_MOYENS2, MORNING_GRANDS3)
//    */
//  test("5 families, 5 days, 3 shift") {
//
//    def mockDay(date:Calendar):List[Shift] = List[Shift](
//      Shift(Shift.NAMES.OPENING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), date, None),
//      Shift(Shift.NAMES.MORNING_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 4.0), date, None),
//      Shift(Shift.NAMES.AFTERNOON_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), date, None)
//    )
//
//
//    var families = mockFamilies(List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin"))
//    val shiftsToResolve = getShiftsForAWeek(Month.NOVEMBER, mockDay).toList
//    println("shiftsToResolve = ", shiftsToResolve)
//
//    val limits = Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
//    val result = ShiftManager.resolve(families, shiftsToResolve, limits)
//    println("RESULTS:")
//    result.foreach(f => {
//      println("\nallResult: ".concat(f.id.concat(" family total: ").concat(f.shifts.size.toString)))
//      f.shifts.foreach(s => {
//        println(s.shiftType.id.concat(" : ").concat(s.date.getTime.toString))
//      })
//    })
//
//    assert(families
//      .filter(f => f.shifts.size != 3)
//      .filter(f => f.shifts.filter(s => s.shiftType.shiftType == Shift.TYPES.GUARD) != 2)
//      .filter(f => f.shifts.filter(s => s.shiftType.shiftType == Shift.TYPES.ORGANISE) != 1)
//      .isEmpty
//    )
//  }
//
//  test("november 2019") {
//
//    val MONTH = Month.NOVEMBER
//
//    var fMoyens = mockFamilies(List("EMMA","LAUTARO","AIMÉE","JULIETTE","LOUIS","ÉLISA","MARCEAU"))
//    val mShiftsToResolve = getShiftsInMonth(MONTH, moyenDay).toList.sortBy(_._1)
//    var fGrands = mockFamilies(List("LOUISE","ROMY","TIAGO","RUBEN","RAPHAËL","SUZANNE"))
//    val gShiftsToResolve = getShiftsInMonth(MONTH, grandDay).toList.sortBy(_._1)
//
//    val limits = Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
//    val moyenResult = ShiftManager.resolve(fMoyens, mShiftsToResolve, limits)
//    val grandResult = ShiftManager.resolve(fGrands, gShiftsToResolve, limits)
//
//    var guardAllResult = moyenResult ::: grandResult
//
//    //      guardAllResult.foreach(f => {
//    //        println("\nguardAllResult: ".concat(f.id.concat(" family total: ").concat(f.shifts.size.toString)))
//    //        f.shifts.foreach(s => {
//    //          println(s.shiftType.id.concat(" : ").concat(s.date.getTime.toString))
//    //        })
//    //      })
//
//    println("Total guardAllResult = " + guardAllResult.map(s => s.shifts.size).sum.toString)
//
//
//    val orgShiftsToResolve = getShiftsInMonth(MONTH, organiseDay).toList.sortBy(_._1)
//    val orgResult = ShiftManager.resolve(guardAllResult, orgShiftsToResolve, limits)
//
//    //    orgResult.foreach(f => {
//    //      println("\norgResult: ".concat(f.id.concat(" family total: ").concat(f.shifts.size.toString)))
//    //      f.shifts.foreach(s => {
//    //        println(s.shiftType.id.concat(" : ").concat(s.date.getTime.toString))
//    //      })
//    //    })
//
//    // combine everything
//    var allResult = guardAllResult.map(f => orgResult.find(of => of.id == f.id) match {
//      case Some(i) => {
//        val out = Family(f.id)
//        var s = f.shifts.toSet
//        s ++= i.shifts
//
//        for(i <- s.toList.sortBy(_.date)) {
//          out.addShift(i)
//        }
//        out
//      }
//      case None => f
//    })
//
//    allResult.foreach(f => {
//      println("\nallResult: ".concat(f.id.concat(" family total: ").concat(f.shifts.size.toString)))
//      f.shifts
//        .sortBy(s => s.date)
//        .foreach(s => {
//          println(s.shiftType.id.concat(" : ").concat(s.date.getTime.toString))
//        })
//    })
//
//    //check no family has a shift on the same day
//    val family_shifts = allResult.map(f =>
//      f.mappedShifts.values
//    ).flatten
//
//    // check no double shifts
//    var pass = true
//    breakable {
//      for(fam <- family_shifts) {
//        breakable {
//          for(shift <- fam) {
//            if(fam.find(s =>  s != shift && DateUtils.sameDay(s.date, shift.date)).isDefined) {
//              println("DOUBLE SHIFT!!! Family: ".concat(fam.toString()))
//              pass = false
//              break
//            }
//          }
//        }
//        if(pass == false) {
//          break
//        }
//      }
//    }
//    assert(pass == true)
//
//    // check shifts don't exceed limits
//    val weeks = DateUtils.getWeeksInMonth(MONTH)
//    allResult.foreach(f => {
//      weeks.foreach(m => {
//        val week = f.shifts.filter(s => s.date.get(Calendar.WEEK_OF_YEAR) == m)
//        assert(week.size < 4
//          && week.filter(s => s.shiftType.shiftType == Shift.TYPES.GUARD).size < 3
//          && week.filter(s => s.shiftType.shiftType == Shift.TYPES.ORGANISE).size < 2
//        )
//      })
//    })
//  }
}
