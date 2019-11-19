import model.{Family, Shift}
import org.scalatest.FunSuite
import services.ShiftManager
import utils.{DateUtils, TestUtils}

class KeepItSimpleTests extends FunSuite {

  test("Balanced") {
    println("\n\n================ Balanced ================ ")
    def mockWeek(n:Int):List[Shift] = TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(0), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(1), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(2), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(3), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(4), n))
    val autoFilledShifts: List[(Shift, Option[Family])] = ShiftManager.autoFill(mockWeek(0)
      , TestUtils.mockFamilies()
      , Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
    )

    println("\n\nAutofill results: ")
    TestUtils.printShifts(autoFilledShifts)
    println("\n")

    val unassignedShifts = autoFilledShifts.filter(s => s._2.isEmpty)
    println("unassigned shifts: ".concat(unassignedShifts.size.toString))

    val assigned = autoFilledShifts.filterNot(unassignedShifts.contains(_))
    println("assigned shifts: ".concat(assigned.size.toString()))

    val out = ShiftManager.resolveUnassigned(unassignedShifts.map(_._1), assigned, List[Shift](), Map((Shift.TYPES.ORGANISE -> 1.5), (Shift.TYPES.GUARD -> 8.0)))

    println("\n\n1st pass resolve unassigned results: ")
    TestUtils.printShifts(out._1.map(s => (s, None)) ::: out._2)
    println("\n")
    println("assigned: ".concat(out._2.size.toString()))
    println("unassigned: ".concat(out._1.size.toString()))

    assert(out._1.size == 0 && out._2.size == 15)
  }

  test("One too many shifts") {
    println("\n\n================ One too many shifts ================ ")
    def mockWeek(n:Int):List[Shift] = TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(0), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(1), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(2), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(3), n)) ::: TestUtils.mockSpecialDay(DateUtils.addWeeks(TestUtils.getDay(4), n))
    val autoFilledShifts: List[(Shift, Option[Family])] = ShiftManager.autoFill(mockWeek(0)
      ,TestUtils.mockFamilies()
      , Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
    )

    println("\n\nAutofill results: ")
    TestUtils.printShifts(autoFilledShifts)
    println("\n")

    val unassignedShifts = autoFilledShifts.filter(s => s._2.isEmpty)
    println("unassigned shifts: ".concat(unassignedShifts.size.toString))

    val assigned = autoFilledShifts.filterNot(unassignedShifts.contains(_))
    println("assigned shifts: ".concat(assigned.size.toString()))

    val out = ShiftManager.resolveUnassigned(unassignedShifts.map(_._1), assigned, List[Shift](), Map((Shift.TYPES.ORGANISE -> 1.5), (Shift.TYPES.GUARD -> 8.0)))

    println("\n\n1st pass resolve unassigned results: ")
    TestUtils.printShifts(out._1.map(s => (s, None)) ::: out._2)
    println("\n")
    println("assigned: ".concat(out._2.size.toString()))
    println("unassigned: ".concat(out._1.size.toString()))

    assert(out._1.size == 1 && out._2.size == 15)
  }


}
