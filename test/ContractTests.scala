import model2.Limits
import org.scalatest.FunSuite

class ContractTests extends FunSuite {
  test("Empty Limits.toList") {
    val limits = Limits(None, None, None).toList.filter(l => l.isDefined)
    assert(limits.size == 0)
  }

  test("Empty daily Limit") {
    val limits = Limits(Option(10), None, None)
    assert(limits.toList.filter(l => l.isDefined).size == 1 && limits.daily.get == 10)
  }

  test("Empty weekly Limit") {
    val limits = Limits(None, Option(10), None)
    assert(limits.toList.filter(l => l.isDefined).size == 1 && limits.weekly.get == 10)
  }

  test("Empty monthly Limit") {

    val i = List(List[String]("OPEN", "CLOSE"), List[String]("OPEN", "CLOSE"))
      .flatten
        .toSet

    println(i)


    val limits = Limits(None, None, Option(10))
    assert(limits.toList.filter(l => l.isDefined).size == 1 && limits.monthly.get == 10)
  }
}
