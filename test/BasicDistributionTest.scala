import java.util.Calendar

import scala.util.Random
import controllers.HomeController
import model.{Family, Shift, ShiftType}
import org.joda.time.DateTime
import org.scalatest.FunSuite
import services.ShiftManager
import utils.DateUtils

import scala.collection.mutable.ListBuffer

class BasicDistributionTest extends FunSuite {

  //TODO - add restrictions / preferences
  //TODO - prioritise families for week if previous week they don't meet weekly quota

  test("remove date") {
    def mockDay(n:Calendar):List[Shift] = List[Shift](
      Shift("o".concat(n.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), n, None),
      Shift("m".concat(n.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
      Shift("a".concat(n.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
      Shift("c".concat(n.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), n, None)
    )

    val anchor: DateTime = new DateTime(2019, 11, 4, 0, 0)
    def getDay(plus:Int) = {
      val d = Calendar.getInstance()
      d.setTime(anchor.toDate)
      d.add(Calendar.DATE, plus)
      d
    }

    def mockWeek():List[Shift] = mockDay(getDay(0)) ::: mockDay(getDay(1)) ::: mockDay(getDay(2)) ::: mockDay(getDay(3)) ::: mockDay(getDay(4))

    def mockFamilies(): List[Family] = {
      val l = Family("Lautaro")
        l.addShift(Shift("toto", ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), getDay(0), Some(l)))
      val e = Family("Emma").addShift(Shift("toto", ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), getDay(1), None))
      val el = Family("Elisa").addShift(Shift("toto", ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), getDay(2), None))
      val g = Family("Gabriel").addShift(Shift("toto", ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), getDay(3), None))
      val f = Family("Florentin").addShift(Shift("toto", ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), getDay(4), None))

     List(l, e, el, g, f)
    }

    val families = mockFamilies()
    val shifts = mockWeek()

    val contenders = ShiftManager.removeByDate(families, getDay(0))
    assert(contenders.exists(f => f.id == "Lautaro") == false)
  }

  test("remove families by shifttype id") {
    def mockDay(n:Calendar):List[Shift] = List[Shift](
      Shift("o".concat(n.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), n, None),
      Shift("m".concat(n.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
      Shift("a".concat(n.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
      Shift("c".concat(n.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), n, None)
    )

    val anchor: DateTime = new DateTime(2019, 11, 4, 0, 0)
    def getDay(plus:Int) = {
      val d = Calendar.getInstance()
      d.setTime(anchor.toDate)
      d.add(Calendar.DATE, plus)
      d
    }

    def mockWeek():List[Shift] = mockDay(getDay(0)) ::: mockDay(getDay(1)) ::: mockDay(getDay(2)) ::: mockDay(getDay(3)) ::: mockDay(getDay(4))

    def mockFamilies(): List[Family] = {
      val l = Family("Lautaro")
      l.addShift(Shift("toto", ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), getDay(0), Some(l)))
      val e = Family("Emma")
        e.addShift(Shift("toto", ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 1.5), getDay(1), Some(e)))
      val el = Family("Elisa")
        el.addShift(Shift("toto", ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 1.5), getDay(2), Some(el)))
      val g = Family("Gabriel")
        g.addShift(Shift("toto", ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 1.5), getDay(3), Some(g)))
      val f = Family("Florentin")
        f.addShift(Shift("toto", ShiftType(Shift.NAMES.AFTERNOON_GRANDS, Shift.TYPES.GUARD, 1.5), getDay(4), Some(f)))
      val a = Family("Aaron")
        a.addShift(Shift("toto", ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), getDay(4), Some(a)))

      List(l, e, el, g, f, a)
    }

    val families = mockFamilies()
    val shifts = mockWeek()

    val noOpening = ShiftManager.removeByShiftId(families, Shift.NAMES.OPENING)
    assert(noOpening.exists(f => f.id == "Lautaro") == false && noOpening.size == 5)

    val noMorningMoyens = ShiftManager.removeByShiftId(noOpening, Shift.NAMES.MORNING_MOYENS)
    assert(noMorningMoyens.exists(f => f.id == "Emma") == false && noMorningMoyens.size == 4)

    val noMorningGrands = ShiftManager.removeByShiftId(noMorningMoyens, Shift.NAMES.MORNING_GRANDS)
    assert(noMorningGrands.exists(f => f.id == "Elisa") == false && noMorningGrands.size == 3)

    val noAfternoonMoyens = ShiftManager.removeByShiftId(noMorningGrands, Shift.NAMES.AFTERNOON_MOYENS)
    assert(noAfternoonMoyens.exists(f => f.id == "Gabriel") == false && noAfternoonMoyens.size == 2)

    val noAfternoonGrands = ShiftManager.removeByShiftId(noAfternoonMoyens, Shift.NAMES.AFTERNOON_GRANDS)
    assert(noAfternoonGrands.exists(f => f.id == "Florentin") == false && noAfternoonGrands.size == 1)

    val noClosing = ShiftManager.removeByShiftId(noAfternoonGrands, Shift.NAMES.CLOSING)
    assert(noClosing.size == 0)
  }

  test("remove by category limit") {
    def mockDay(n:Calendar):List[Shift] = List[Shift](
      Shift("o".concat(n.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), n, None),
      Shift("m".concat(n.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
      Shift("a".concat(n.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
      Shift("c".concat(n.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), n, None)
    )

    val anchor: DateTime = new DateTime(2019, 11, 4, 0, 0)
    def getDay(plus:Int) = {
      val d = Calendar.getInstance()
      d.setTime(anchor.toDate)
      d.add(Calendar.DATE, plus)
      d
    }

    def mockWeek():List[Shift] = mockDay(getDay(0)) ::: mockDay(getDay(1)) ::: mockDay(getDay(2)) ::: mockDay(getDay(3)) ::: mockDay(getDay(4))

    def mockFamilies(): List[Family] = {
      val l = Family("Lautaro")
      l.addShift(Shift("toto", ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), getDay(0), Some(l)))
      val e = Family("Emma")
      e.addShift(Shift("toto", ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 1.5), getDay(1), Some(e)))
      val el = Family("Elisa")
      el.addShift(Shift("toto", ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 1.5), getDay(2), Some(el)))
      val g = Family("Gabriel")
      g.addShift(Shift("toto", ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 1.5), getDay(3), Some(g)))
      val f = Family("Florentin")
      f.addShift(Shift("toto", ShiftType(Shift.NAMES.AFTERNOON_GRANDS, Shift.TYPES.GUARD, 1.5), getDay(4), Some(f)))
      val a = Family("Aaron")
      a.addShift(Shift("toto", ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), getDay(4), Some(a)))

      List(l, e, el, g, f, a)
    }

    val families = mockFamilies()
    val shifts = mockWeek()

    val toto = ShiftManager.removeByShiftCategory(families, ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), 1)
    assert(toto.exists(f => f.id == "Lautaro") == false)
    assert(toto.exists(f => f.id == "Aaron") == false)

    val tata = ShiftManager.removeByShiftCategory(families, ShiftType("doesn't matter", Shift.TYPES.GUARD, 1.5), 2)
    assert(tata.exists(f => f.id == "Emma") == true)

    val emma = families.find(f => f.id == "Emma").get
    emma.addShift(Shift("toto", ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 4), getDay(1), Some(emma)))
    val tata2 = ShiftManager.removeByShiftCategory(families, ShiftType("doesn't matter", Shift.TYPES.GUARD, 1.5), 2)
    assert(tata2.exists(f => f.id == "Emma") == false)

    val titi = ShiftManager.removeByShiftCategory(families, ShiftType("doesn't matter", Shift.TYPES.GUARD, 1.5), 2)
    assert(titi.exists(f => f.id == "Emma") == false)
  }

  test("Basic autoFill week") {
    def mockDay(n:Calendar):List[Shift] = List[Shift](
      Shift("o".concat(n.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), n, None),
      Shift("m".concat(n.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
      Shift("a".concat(n.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
      Shift("c".concat(n.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), n, None)
    )

    val anchor: DateTime = new DateTime(2019, 11, 4, 0, 0)
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

    val families = mockFamilies()
    val shifts = mockWeek() //::: mockWeek(7) ::: mockWeek(14)

    val out: List[(Shift, Option[Family])] = ShiftManager.autoFill(shifts, families)
//    for(s <- out) {
//      println("Shift:" + s._2.getOrElse("None"))
//    }

//    for(fam <- families.filter(f => f.shifts.toList.size > 3)) {
//      println("Family:" + fam)
//    }

    assert(out.size == 20)
//    assert(!families.exists(f => f.shifts.size > 3))
  }

  test("Basic autoFill 2 weeks") {
    def mockDay(n:Calendar):List[Shift] = List[Shift](
      Shift("o".concat(n.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), n, None),
      Shift("m".concat(n.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
      Shift("a".concat(n.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), n, None),
      Shift("c".concat(n.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), n, None)
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

    val families = mockFamilies()
    val shifts = mockWeek(0) //::: mockWeek(7) ::: mockWeek(14)

    val out: List[(Shift, Option[Family])] = ShiftManager.autoFill(mockWeek(0), Random.shuffle(mockFamilies())) ::: ShiftManager.autoFill(mockWeek(1), Random.shuffle(mockFamilies()))
//        for(s <- out) {
//          println("Shift:" + s._2.getOrElse("None"))
//        }

//    for(fam <- families.filter(f => f.shifts.toList.size > 3)) {
//      println("Family:" + fam)
//    }

        assert(!families.exists(f => f.shifts.size > 3))
  }
//
  test("2 family guard autoFill") {
    def mockDay(date:Calendar):List[Shift] = List[Shift](
      Shift(Shift.NAMES.OPENING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), date, None),
      Shift(Shift.NAMES.MORNING_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), date, None),
      Shift(Shift.NAMES.MORNING_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 4.0), date, None),
      Shift(Shift.NAMES.AFTERNOON_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), date, None),
      Shift(Shift.NAMES.AFTERNOON_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_GRANDS, Shift.TYPES.GUARD, 4.0), date, None),
      Shift(Shift.NAMES.CLOSING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), date, None)
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
//      val names = List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin")
      val names = List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin", "Aaron", "Francisca", "Timothy")
      names.map(f => Family(f))
    }

    val families = mockFamilies()
    val shifts = mockWeek(0)

    val out: List[(Shift, Option[Family])] = ShiftManager.autoFill(shifts, families)
//    for(s <- out) {
//      println("Shift:" + s._2.getOrElse("None") + ", category = " + s._1.shiftType.shiftType)
//    }
//
//    for(f <- families) {
//      println("\n".concat(f.id))
//      for(s <- f.shifts) {
//        println(s.shiftType.shiftType.concat(" - ").concat(s.shiftType.id).concat(" - ").concat(s.date.toString))
//      }
//    }
    assert(out.size == 30)
    assert(!families.exists(f => f.shifts.size > 3))
    assert(!families.exists(f => f.shifts.size == 0))
    assert(!families.exists(f => f.shifts.count(s => s.shiftType.shiftType == Shift.TYPES.ORGANISE) > 1))
    assert(!families.exists(f => f.shifts.count(s => s.shiftType.shiftType == Shift.TYPES.GUARD) > 2))
    //TODO write assert that no family has a >1 shift on the same day
  }

  test("2 family guard, 3 weeks autoFill") {
    def mockDay(date:Calendar):List[Shift] = List[Shift](
      Shift(Shift.NAMES.OPENING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.OPENING, Shift.TYPES.ORGANISE, 1.5), date, None),
      Shift(Shift.NAMES.MORNING_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_MOYENS, Shift.TYPES.GUARD, 4.0), date, None),
      Shift(Shift.NAMES.MORNING_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.MORNING_GRANDS, Shift.TYPES.GUARD, 4.0), date, None),
      Shift(Shift.NAMES.AFTERNOON_MOYENS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_MOYENS, Shift.TYPES.GUARD, 4.0), date, None),
      Shift(Shift.NAMES.AFTERNOON_GRANDS.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.AFTERNOON_GRANDS, Shift.TYPES.GUARD, 4.0), date, None),
      Shift(Shift.NAMES.CLOSING.concat(date.getTime.getDay.toString), ShiftType(Shift.NAMES.CLOSING, Shift.TYPES.ORGANISE, 1.5), date, None)
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
      //      val names = List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin")
      val names = List("Lautaro", "Emma", "Elisa", "Gabriel", "Florentin", "Aaron", "Francisca", "Timothy")
      names.map(f => Family(f))
    }

//    val families = mockFamilies()
//    val shifts = mockWeek(0)

    val out: List[(Shift, Option[Family])] = ShiftManager.autoFill(mockWeek(0), Random.shuffle(mockFamilies())) ::: ShiftManager.autoFill(mockWeek(1), Random.shuffle(mockFamilies())) ::: ShiftManager.autoFill(mockWeek(2), Random.shuffle(mockFamilies()))
//    for(s <- out) {
//      println("Shift:" + s)
//    }

    //TODO - from out, get families by name and create list of their shifts
    def f(shifts:List[(Shift, Option[Family])]):Map[Family, List[Shift]] = {
      var o = Map[Family, List[Shift]]()
      val l = shifts.groupBy(_._2).toList
      for(g <- l) {
        g._1 match {
          case Some(value) => {
            println(value.id.concat(" has ").concat(g._2.size.toString).concat(" ").concat(g._2.map(i => i._1.shiftType.id).toString()))
//            (value, g._2.map(i => i._1))
            o += (value -> g._2.map(i => i._1))
          }
          case None => println("No family")
        }
      }
      o
    }

    for(i <- f(out)) {
      println(i._1)
    }

//    for(f <- families) {
//      println("\n".concat(f.id))
//      for(s <- f.shifts) {
//        println(s.shiftType.shiftType.concat(" - ").concat(s.shiftType.id).concat(" - ").concat(s.date.toString))
//      }
//    }

//    println("TOTO: " + families.count(f => f.id == "Lautaro"))

//    assert(out.size == 90)
//    assert(!families.exists(f => f.shifts.size > 3))
//    assert(!families.exists(f => f.shifts.size == 0))
//    assert(!families.exists(f => f.shifts.count(s => s.shiftType.shiftType == Shift.TYPES.ORGANISE) > 1))
//    assert(!families.exists(f => f.shifts.count(s => s.shiftType.shiftType == Shift.TYPES.GUARD) > 2))
    //TODO write assert that no family has a >1 shift on the same day
  }
}
