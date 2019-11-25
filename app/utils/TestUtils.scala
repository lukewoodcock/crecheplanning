package utils

import java.time.LocalDate
import java.util.Calendar

import model.{Family, Shift, ShiftType}
import org.joda.time.DateTime

object TestUtils {


  def mockDay(date:Calendar):List[Shift] = List[Shift](
    Shift(Shift.NAMES.OPENING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), date, None),
    Shift(Shift.NAMES.MORNING_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 4.0), date, None),
    Shift(Shift.NAMES.AFTERNOON_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), date, None)
  )

  def mockSpecialDay(date:Calendar):List[Shift] = List[Shift](
    Shift(Shift.NAMES.OPENING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), date, None),
    Shift(Shift.NAMES.MORNING_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 4.0), date, None),
    Shift(Shift.NAMES.AFTERNOON_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), date, None),
    Shift(Shift.NAMES.AFTERNOON_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_GRANDS, Shift.TYPES.GUARD, 4.0), date, None)
  )

  val anchor: DateTime = new DateTime(2019, 11, 4, 0, 0)
  def getDay(plus:Int) = {
    val d = Calendar.getInstance()
    d.setTime(anchor.toDate)
    d.add(Calendar.DATE, plus)
    d
  }



  def mockFamilies(): List[Family] = {
    val names = List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin")
    names.map(f => Family(f))
  }

  def printShifts(shifts:List[(Shift, Option[Family])]) = {
    for(g <- shifts.groupBy(_._2).toList) {
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
  }

}
