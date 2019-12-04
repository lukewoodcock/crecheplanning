import model.{Family, Shift}
import org.scalatest.FunSuite
import services.ShiftManager
import utils.{DateUtils, TestUtils}

class KeepItSimpleTests extends FunSuite {

  test("Balanced") {
    def mockWeek(n:Int):List[Shift] = TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(0), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(1), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(2), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(3), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(4), n))
    val out = ShiftManager.autoFillWeek(mockWeek(0)
      , TestUtils.mockFamilies()
      , Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
      , Option("\n\n================ Balanced ================ ")
    )
    assert(out._1.size == 0 && out._2.size == 15)
  }

  ignore("One too many shifts") {
    def mockWeek(n:Int):List[Shift] = TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(0), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(1), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(2), n)) ::: TestUtils.mockDay(DateUtils.addWeeks(TestUtils.getDay(3), n)) ::: TestUtils.mockSpecialDay(DateUtils.addWeeks(TestUtils.getDay(4), n))
    val out = ShiftManager.autoFillWeek(mockWeek(0)
      , TestUtils.mockFamilies()
      , Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
      , Option("\n\n================ One too many shifts ================ ")
    )
    assert(out._1.size == 1 && out._2.size == 15)
  }

}
