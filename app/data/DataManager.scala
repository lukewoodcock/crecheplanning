package data

import java.util.Calendar

import model.{Family, Shift, ShiftType}

object DataManager {}

class DataManager {

  def mockDay(n:Int):List[Shift] = List[Shift](
    Shift("o".concat(n.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), n, None),
    Shift("m".concat(n.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
    Shift("a".concat(n.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
    Shift("c".concat(n.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), n, None)
  )

  def mockWeek():List[Shift] = mockDay(Calendar.MONDAY) ::: mockDay(Calendar.TUESDAY) ::: mockDay(Calendar.WEDNESDAY) ::: mockDay(Calendar.THURSDAY) ::: mockDay(Calendar.FRIDAY)

  def mockFamilies(): List[Family] = {
    val names = List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin")
    names.map(f => Family(f))
  }
}
