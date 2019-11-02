import java.util.Calendar

import controllers.HomeController
import model.{Family, Shift, ShiftType}
import org.scalatest.FunSuite
import services.ShiftManager

class BasicDistributionTest extends FunSuite {
  def mockDay(n:Int):List[Shift] = List[Shift](
    Shift("o".concat(n.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), n, None),
    Shift("m".concat(n.toString), ShiftType(Shift.NAMES.MORNING, Shift.TYPES.GUARD, 4.0), n, None),
    Shift("a".concat(n.toString), ShiftType(Shift.NAMES.AFTERNOON, Shift.TYPES.GUARD, 4.0), n, None),
    Shift("c".concat(n.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), n, None)
  )

  def mockWeek():List[Shift] = mockDay(Calendar.MONDAY) ::: mockDay(Calendar.TUESDAY) ::: mockDay(Calendar.WEDNESDAY) ::: mockDay(Calendar.THURSDAY) ::: mockDay(Calendar.FRIDAY)

  def mockFamilies(): List[Family] = {
    val names = List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin")
    names.map(f => Family(f))
  }

  val out: List[(Shift, Option[Family])] = ShiftManager.autoFill(mockWeek(), mockFamilies())
  for(s <- out) {
    println("Shift:" + s)
  }

  test("autoFill") {
    assert(out.size == 20)
  }
}
