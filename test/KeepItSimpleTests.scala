import java.util.Calendar

import model.{Family, Shift, ShiftType}
import org.joda.time.DateTime
import org.scalatest.FunSuite
import services.ShiftManager
import utils.DateUtils

import scala.util.control.Breaks._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class KeepItSimpleTests extends FunSuite {
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

  val criteria = Map((Shift.TYPES.ORGANISE -> 1.5), (Shift.TYPES.GUARD -> 8.0))

  /**
    * Sums the duration of shifts by type and checks against max durations for each shift type
    * @param shifts List of shifts to sum
    * @param limits Map of max durations Key: shiftType, Value: Max duration
    * @return true if limits not exceeded, otherwise false
    */
  def isUnderMaxShiftDurations(shifts:List[Shift], limits:Map[String, Double]):Boolean = {
    var out = true
    breakable {
      for(c <- limits) {
        val t = shifts.filter(s => s.shiftType.shiftType == c._1).map(_.shiftType.duration).sum
        if(t > c._2) {
          out = false
          break
        }
      }
    }
    out
  }

  /**
    * Recursive function that tries to resolve the 'unassigned' shifts parameter.
    *
    * 1. It searches the list of assigned shifts and checks the families' shifts to see if it can be assigned to the family give a 'criteria'
    * TODO: optimise checking families
    * TODO: add to criteria - no same date
    *
    * @param unassigned
    * @param assigned
    * @param unresolved
    * @param criteria
    * @return
    */
  def resolveUnassigned2(unassigned:List[Shift], assigned:List[(Shift, Option[Family])], unresolved:List[Shift], criteria:Map[String, Double]):(List[Shift], List[(Shift, Option[Family])]) = {

    //get all unassigned shifts
    unassigned match {
      case head :: tail => {
        //          println("======= head :: tail - " + tail.size)
        val shiftType = head.shiftType.shiftType

        // filter assigned shifts by shift type and whose family has not exceed duration for that shift
        val filteredAndSortedAssigned = assigned.filter(s => s._1.shiftType.shiftType == shiftType)
          .filter(f => {
            f._2.get.shifts.filter(s => s.shiftType.shiftType == shiftType)
              .map(_.shiftType.duration).sum <= criteria.getOrElse(shiftType, 0.0)
          })
          .sortBy(_._2.get.shifts.filter(s => s.shiftType.shiftType == shiftType)
            .map(_.shiftType.duration).sum)

        var a = assigned

        breakable {
          for(s <- filteredAndSortedAssigned) {
            // can family add shift?
            val f = s._2.get.shifts.toList ::: List(head)
            if(isUnderMaxShiftDurations(f, criteria) == true) {
              //                println("Pick")
              s._2.get.addShift(head)
              a = a :+ (head, s._2)
              break
            } else {
              // TODO - try swap, if yes do swap, concat other to tail and call function
              // TODO - will need to prevent infinite swapping
            }
          }
        }
        val resolved = a

        if(resolved.size > assigned.size) {
          resolveUnassigned2(tail, resolved, unresolved, criteria)
        } else {
          resolveUnassigned2(tail, assigned, unresolved ::: List(head), criteria)
        }
      }
      case Nil => {
        //          println("======= Nil")
        (unresolved, assigned)
      }
    }

    /**
      * first pass - simple pass to fix or be implemented into autoFill
      * for unassigned shift-i
      * find families with space on their agenda
      * try to assign shift, otherwise store as unresolved
      *
      * also need list of families with space
      *
      * second pass - create list of swappable
      * for unresolved shift-i
      * find all shifts of same type
      * try
      */
  }

  test("Balanced") {
    println("\n\n================ Balanced ================ ")
    def mockWeek(n:Int):List[Shift] = mockDay(DateUtils.addWeeks(getDay(0), n)) ::: mockDay(DateUtils.addWeeks(getDay(1), n)) ::: mockDay(DateUtils.addWeeks(getDay(2), n)) ::: mockDay(DateUtils.addWeeks(getDay(3), n)) ::: mockDay(DateUtils.addWeeks(getDay(4), n))
    val autoFilledShifts: List[(Shift, Option[Family])] = ShiftManager.autoFill(mockWeek(0)
      , mockFamilies()
      , Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
    )

    println("\n\nAutofill results: ")
    printShifts(autoFilledShifts)
    println("\n")

    val unassignedShifts = autoFilledShifts.filter(s => s._2.isEmpty)
    println("unassigned shifts: ".concat(unassignedShifts.size.toString))

    val assigned = autoFilledShifts.filterNot(unassignedShifts.contains(_))
    println("assigned shifts: ".concat(assigned.size.toString()))

    val out = resolveUnassigned2(unassignedShifts.map(_._1), assigned, List[Shift](), criteria)

    println("\n\n1st pass resolve unassigned results: ")
    printShifts(out._1.map(s => (s, None)) ::: out._2)
    println("\n")
    println("assigned: ".concat(out._2.size.toString()))
    println("unassigned: ".concat(out._1.size.toString()))

    assert(out._1.size == 0 && out._2.size == 15)
  }

  test("One too many shifts") {
    println("\n\n================ One too many shifts ================ ")
    def mockWeek(n:Int):List[Shift] = mockDay(DateUtils.addWeeks(getDay(0), n)) ::: mockDay(DateUtils.addWeeks(getDay(1), n)) ::: mockDay(DateUtils.addWeeks(getDay(2), n)) ::: mockDay(DateUtils.addWeeks(getDay(3), n)) ::: mockSpecialDay(DateUtils.addWeeks(getDay(4), n))
    val autoFilledShifts: List[(Shift, Option[Family])] = ShiftManager.autoFill(mockWeek(0)
      , mockFamilies()
      , Map((Shift.TYPES.GUARD, 2),(Shift.TYPES.ORGANISE, 1))
    )

    println("\n\nAutofill results: ")
    printShifts(autoFilledShifts)
    println("\n")

    val unassignedShifts = autoFilledShifts.filter(s => s._2.isEmpty)
    println("unassigned shifts: ".concat(unassignedShifts.size.toString))

    val assigned = autoFilledShifts.filterNot(unassignedShifts.contains(_))
    println("assigned shifts: ".concat(assigned.size.toString()))

    val out = resolveUnassigned2(unassignedShifts.map(_._1), assigned, List[Shift](), criteria)

    println("\n\n1st pass resolve unassigned results: ")
    printShifts(out._1.map(s => (s, None)) ::: out._2)
    println("\n")
    println("assigned: ".concat(out._2.size.toString()))
    println("unassigned: ".concat(out._1.size.toString()))

    assert(out._1.size == 1 && out._2.size == 15)
  }


}
