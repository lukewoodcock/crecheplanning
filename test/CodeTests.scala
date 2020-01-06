import java.time.{DayOfWeek, LocalDate, Month, Year}
import java.util.Calendar

import model.{Family, Shift, ShiftType}
import org.joda.time.DateTime
import org.scalatest.FunSuite
import services.ShiftManager
import utils.DateUtils.getMonth
import utils.{DateUtils, TestUtils}

import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks.{break, breakable}


class CodeTests extends FunSuite {

  //TODO - add restrictions / preferences
  //TODO - prioritise families for week if previous week they don't meet weekly quota

  def mockMonth(month:Month):List[Shift] = {
    val m = DateUtils.getMonth(Month.NOVEMBER)
    val out = ListBuffer[List[Shift]]()
    for(d <- m) {
      out += TestUtils.mockDay(d)
    }
    out.toList.flatten
  }

  def mockWeek(month:Month):List[Shift] = {
    val m = DateUtils.getMonth(Month.NOVEMBER)
    val w1 = m.map(c => c.get(Calendar.WEEK_OF_YEAR))
    val out = ListBuffer[List[Shift]]()
    for(d <- m) {
      out += TestUtils.mockDay(d)
    }
    out.toList.flatten
  }

  /**
    * ==== Moyens
    * EMMA
    * LAUTARO
    * AIMÉE
    * JULIETTE
    * LOUIS
    * ÉLISA
    * MARCEAU
    *
    * ==== Grands
    * LOUISE
    * ROMY
    * TIAGO
    * RUBEN
    * RAPHAËL
    * SUZANNE
    */
  def mockFamilies(names:List[String]): List[Family] = names.map(f => Family(f))

  def moyenDay(date:Calendar) = List[Shift](
    Shift(Shift.NAMES.MORNING_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), date, None),
    Shift(Shift.NAMES.AFTERNOON_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), date, None)
  )

  def grandDay(date:Calendar) = List[Shift](
    Shift(Shift.NAMES.MORNING_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 4.0), date, None),
    Shift(Shift.NAMES.AFTERNOON_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_GRANDS, Shift.TYPES.GUARD, 4.0), date, None)
  )

  def organiseDay(date:Calendar) = List[Shift](
    Shift(Shift.NAMES.OPENING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), date, None),
    Shift(Shift.NAMES.CLOSING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), date, None)
  )

  def getWeeksInMonth(month:Month) = getMonth(month).groupBy(_.get(Calendar.WEEK_OF_YEAR))

  def getShiftsInMonth(month:Month, shiftDefinition:(Calendar) => List[Shift]) = {
    val weeks = getWeeksInMonth(month)
    weeks.map(w => {
      (w._1 -> {
        w._2.map(d => shiftDefinition(d)).toList.flatten
      })
    })
  }

  def getShiftsForAWeek(month:Month, shiftDefinition:(Calendar) => List[Shift]) = {

    val w = getWeeksInMonth(month)
      .filter(i => i._2.size == 5)
    val weeks = getShiftsInMonth(month, shiftDefinition)
    weeks
      .filter(i => i._1 == w.head._1)
  }

  test("1 family, 1 day") {
    var fMoyens = mockFamilies(List("EMMA"))
    val firstWeekM = getShiftsInMonth(Month.NOVEMBER, moyenDay).toList.sortBy(_._1).head
    val mShiftsToResolve = Map(firstWeekM._1 -> firstWeekM._2.filter(s => s.date == firstWeekM._2.head.date)).toList
    val firstWeekO = getShiftsInMonth(Month.NOVEMBER, organiseDay).toList.sortBy(_._1).head
    val orgShiftsToResolve = Map(firstWeekO._1 -> firstWeekO._2.filter(s => s.date == firstWeekM._2.head.date)).toList


//    println("mShiftsToResolve = ", mShiftsToResolve.map(s => s._2.map(i => i.toString())))
    println("mShiftsToResolve = ", mShiftsToResolve)

    val limits = Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
    val moyenResult = ShiftManager.resolve(fMoyens, mShiftsToResolve, limits)
//    println("moyenResult: " + moyenResult)

    val orgResult = ShiftManager.resolve(moyenResult, orgShiftsToResolve, limits)
//    println("orgResult: " + orgResult)


    println("RESULTS:")
    moyenResult.foreach(f => {
      println("\nallResult: ".concat(f.id.concat(" family total: ").concat(f.shifts.size.toString)))
      f.shifts.foreach(s => {
        println(s.shiftType.id.concat(" : ").concat(s.date.getTime.toString))
      })
    })

    (mShiftsToResolve ::: orgShiftsToResolve).toMap.values.flatten.foreach(s => println(s))
    assert(fMoyens.find(f => f.id == "EMMA").get.shifts.size == 1)
  }

  /**
    * Gabriel has 3 List(OPENING2, AFTERNOON_MOYENS3, MORNING_GRANDS4)
    * Elisa has 3 List(AFTERNOON_MOYENS1, OPENING4, MORNING_GRANDS5)
    * Emma has 3 List(MORNING_GRANDS1, OPENING3, AFTERNOON_MOYENS4)
    * Florentin has 2 List(MORNING_GRANDS2, OPENING5)
    * No family AFTERNOON_MOYENS5
    * Lautaro has 3 List(OPENING1, AFTERNOON_MOYENS2, MORNING_GRANDS3)
    */
  test("5 families, 5 days, 3 shift") {

    def mockDay(date:Calendar):List[Shift] = List[Shift](
      Shift(Shift.NAMES.OPENING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), date, None),
      Shift(Shift.NAMES.MORNING_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 4.0), date, None),
      Shift(Shift.NAMES.AFTERNOON_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), date, None)
    )


    var families = mockFamilies(List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin"))
    val shiftsToResolve = getShiftsForAWeek(Month.NOVEMBER, mockDay).toList
    println("shiftsToResolve = ", shiftsToResolve)

    val limits = Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
    val result = ShiftManager.resolve(families, shiftsToResolve, limits)
    println("RESULTS:")
    result.foreach(f => {
      println("\nallResult: ".concat(f.id.concat(" family total: ").concat(f.shifts.size.toString)))
      f.shifts.foreach(s => {
        println(s.shiftType.id.concat(" : ").concat(s.date.getTime.toString))
      })
    })

    assert(families
        .filter(f => f.shifts.size != 3)
        .filter(f => f.shifts.filter(s => s.shiftType.shiftType == Shift.TYPES.GUARD) != 2)
        .filter(f => f.shifts.filter(s => s.shiftType.shiftType == Shift.TYPES.ORGANISE) != 1)
        .isEmpty
    )
  }

  test("november 2019") {

    val MONTH = Month.NOVEMBER

    var fMoyens = mockFamilies(List("EMMA","LAUTARO","AIMÉE","JULIETTE","LOUIS","ÉLISA","MARCEAU"))
    val mShiftsToResolve = getShiftsInMonth(MONTH, moyenDay).toList.sortBy(_._1)
    var fGrands = mockFamilies(List("LOUISE","ROMY","TIAGO","RUBEN","RAPHAËL","SUZANNE"))
    val gShiftsToResolve = getShiftsInMonth(MONTH, grandDay).toList.sortBy(_._1)

    val limits = Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
    val moyenResult = ShiftManager.resolve(fMoyens, mShiftsToResolve, limits)
    val grandResult = ShiftManager.resolve(fGrands, gShiftsToResolve, limits)

    var guardAllResult = moyenResult ::: grandResult

//      guardAllResult.foreach(f => {
//        println("\nguardAllResult: ".concat(f.id.concat(" family total: ").concat(f.shifts.size.toString)))
//        f.shifts.foreach(s => {
//          println(s.shiftType.id.concat(" : ").concat(s.date.getTime.toString))
//        })
//      })

    println("Total guardAllResult = " + guardAllResult.map(s => s.shifts.size).sum.toString)


    val orgShiftsToResolve = getShiftsInMonth(MONTH, organiseDay).toList.sortBy(_._1)
    val orgResult = ShiftManager.resolve(guardAllResult, orgShiftsToResolve, limits)

//    orgResult.foreach(f => {
//      println("\norgResult: ".concat(f.id.concat(" family total: ").concat(f.shifts.size.toString)))
//      f.shifts.foreach(s => {
//        println(s.shiftType.id.concat(" : ").concat(s.date.getTime.toString))
//      })
//    })

    // combine everything
    var allResult = guardAllResult.map(f => orgResult.find(of => of.id == f.id) match {
      case Some(i) => {
        val out = Family(f.id)
        var s = f.shifts.toSet
        s ++= i.shifts

        for(i <- s.toList.sortBy(_.date)) {
          out.addShift(i)
        }
        out
      }
      case None => f
    })

    allResult.foreach(f => {
      println("\nallResult: ".concat(f.id.concat(" family total: ").concat(f.shifts.size.toString)))
      f.shifts
          .sortBy(s => s.date)
        .foreach(s => {
        println(s.shiftType.id.concat(" : ").concat(s.date.getTime.toString))
      })
    })

    //check no family has a shift on the same day
    val family_shifts = allResult.map(f =>
      f.mappedShifts.values
    ).flatten

    // check no double shifts
    var pass = true
    breakable {
      for(fam <- family_shifts) {
        breakable {
          for(shift <- fam) {
            if(fam.find(s =>  s != shift && DateUtils.sameDay(s.date, shift.date)).isDefined) {
              println("DOUBLE SHIFT!!! Family: ".concat(fam.toString()))
              pass = false
              break
            }
          }
        }
        if(pass == false) {
          break
        }
      }
    }
    assert(pass == true)

    // check shifts don't exceed limits
    val weeks = DateUtils.getWeeksInMonth(MONTH)
    allResult.foreach(f => {
      weeks.foreach(m => {
        val week = f.shifts.filter(s => s.date.get(Calendar.WEEK_OF_YEAR) == m)
        assert(week.size < 4
          && week.filter(s => s.shiftType.shiftType == Shift.TYPES.GUARD).size < 3
          && week.filter(s => s.shiftType.shiftType == Shift.TYPES.ORGANISE).size < 2
        )
      })
    })
  }
}
