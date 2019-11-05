package data

import java.util.Calendar

import model.{Family, Shift, ShiftType}
import org.joda.time.DateTime

object DataManager {}

class DataManager {

  def mockDay(n:Calendar):List[Shift] = List[Shift](
    Shift("o".concat(n.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), n, None),
    Shift("m".concat(n.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
    Shift("a".concat(n.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
    Shift("c".concat(n.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), n, None)
  )

  val anchor: DateTime = new DateTime(2019, 11, 4)
  def getDay(plus:Int) = {
    val d = Calendar.getInstance()
    d.setTime(anchor.toDate)
    d.add(Calendar.DATE, plus)
    d
  }

  def mockWeek():List[Shift] = mockDay(getDay(0)) ::: mockDay(getDay(1)) ::: mockDay(getDay(2)) ::: mockDay(getDay(3)) ::: mockDay(getDay(4))

  def mockFamilies(): List[Family] = {
    val names = List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin")
    names.map(f => Family(f))
  }
}
