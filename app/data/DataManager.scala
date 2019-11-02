package data

import java.util.Calendar

import model.{Family, Shift, ShiftType}

object DataManager {}

class DataManager {

  def mockDay(n:Int):List[Shift] = List[Shift](
    Shift("o".concat(n.toString), ShiftType(Shift.OPENING, 1.5), n, None),
    Shift("m".concat(n.toString), ShiftType(Shift.MORNING, 4.0), n, None),
    Shift("a".concat(n.toString), ShiftType(Shift.AFTERNOON, 4.0), n, None),
    Shift("c".concat(n.toString), ShiftType(Shift.CLOSING, 1.5), n, None)
  )

  def mockWeek():List[Shift] = mockDay(Calendar.MONDAY) ::: mockDay(Calendar.TUESDAY) ::: mockDay(Calendar.WEDNESDAY) ::: mockDay(Calendar.THURSDAY) ::: mockDay(Calendar.FRIDAY)

  def mockFamilies() = {
    val names = List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin")
//    names.map(f => Some(Family(f)))
    names.map(f => Family(f))
  }
}
