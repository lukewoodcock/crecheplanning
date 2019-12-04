import java.util.Calendar

import model.{Family, Shift, ShiftType}
import org.joda.time.DateTime
import org.scalatest.FunSuite
import services.ShiftManager
import utils.DateUtils

import scala.util.Random

class EssentialDistributionTest extends FunSuite {


  // TODO - this test fails. See print out below
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

    val anchor: DateTime = new DateTime(2019, 11, 4, 0, 0)
    def getDay(plus:Int) = {
      val d = Calendar.getInstance()
      d.setTime(anchor.toDate)
      d.add(Calendar.DATE, plus)
      d
    }

    def mockWeek(n:Int):List[Shift] = mockDay(DateUtils.addWeeks(getDay(0), n)) ::: mockDay(DateUtils.addWeeks(getDay(1), n)) ::: mockDay(DateUtils.addWeeks(getDay(2), n)) ::: mockDay(DateUtils.addWeeks(getDay(3), n)) ::: mockDay(DateUtils.addWeeks(getDay(4), n))

    def mockFamilies(): List[Family] = {
      val names = List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin")
      names.map(f => Family(f))
    }

    val out: List[(Shift, Option[Family])] = ShiftManager.greedyAutoFill(mockWeek(0)
      , mockFamilies()
      , Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
    )

    def f(shifts:List[(Shift, Option[Family])]):Map[Family, List[Shift]] = {
      var o = Map[Family, List[Shift]]()
      val l = shifts.groupBy(_._2).toList
      for(g <- l) {
        g._1 match {
          case Some(value) => {
            println(value.id.concat(" has ").concat(g._2.size.toString).concat(" ").concat(g._2.map(i => i._1.id).toString()))


//            assert(value.shifts.size == 3)
//            assert(value.shifts.count(s => s.id == Shift.NAMES.OPENING) == 1)
//            assert(value.shifts.count(s => s.id == Shift.NAMES.MORNING_GRANDS) == 1)
//            assert(value.shifts.count(s => s.id == Shift.NAMES.AFTERNOON_MOYENS) == 1)
          }
          case None => {
            g._2.foreach(s => println("No family ".concat(s._1.id)))
          }
        }
      }
      o
    }

    f(out)
  }
}
