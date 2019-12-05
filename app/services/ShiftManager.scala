package services

import java.util.Calendar

import model.{Family, Shift, ShiftType}
import utils.{DateUtils, TestUtils}

import scala.util.control.Breaks.{break, breakable}

object ShiftManager {

  def removeByDate(families:List[Family], date:Calendar): List[Family] = families.filter(f =>
    !f.shifts.toList.exists(fs => DateUtils.sameDay(fs.date, date))
  )

  def removeByShiftId(families:List[Family], id:String): List[Family] = families.filter(f =>
    !f.shifts.toList.exists(fs => fs.shiftType.id.equals(id))
  )

  def removeByShiftCategory(families:List[Family], shiftType:ShiftType, limit:Int): List[Family] = families.filter(f =>
    f.shifts.count(s => s.shiftType match {
      case ShiftType(_, shiftType, _) => true
      case _ => false
    }) < limit
  )

  /**
    *
    * @param _families
    * @param shiftsToResolve - tuple where _1 is the week of the year and _2 is the shifts in the week
    * @return
    */
  def resolve(_families:List[Family], shiftsToResolve:List[(Int, List[Shift])], limits:Map[String, Int]) = {
    var families = _families
    var results:(List[Shift], List[(Shift, Option[Family])]) = (List[Shift](), List[(Shift, Option[Family])]())
    for(toto <- shiftsToResolve) {
      val it = ShiftManager.autoFillWeek(toto._2
        , families
        , limits
        //                  , Option("\n\n================ week ".concat(toto._1.toString).concat("================ "))
      )
      results = (results._1 ::: it._1, results._2 ::: it._2)
      val af = families.sortBy(_.shifts.size)
      families = af
    }

    val all = results._2
      .filter(r => r._2 match {
        case Some(fam) => true
        case None => false
      })
      .groupBy(_._2.get.id)

    //    println("All : ".concat(all.toString()))
    //    all.foreach(r => {
    //      println(r._1.concat(" has ").concat(r._2.size.toString).concat(" shifts"))
    //      println(r._2.map(s => s._1.id))
    //    })

    all.map(r => {
      val f = Family(r._1)
      r._2.foreach(s => {
        f.addShift(s._1)
      })
      f
    }).toList
  }

  /**
    * Assigns Family to Shifts
    *
    * @param shifts List of Shift to be assigned
    * @param families List of Family to be assigned
    * @param limits
    * @return ._1 a list of unresolved (unassigned shifts), ._2 list of tuples where ._1 is an assigned shift and ._2 is Some(Family)
    */
  def autoFillWeek(shifts:List[Shift], families:List[Family], limits:Map[String, Int], printProcess:Option[String] = None) : (List[Shift], List[(Shift, Option[Family])]) = {
    printProcess match {
      case Some(s) => {
//        println(s)
        val autoFilledShifts = greedyAutoFill(shifts, families, limits)

        println("\n\nAutofill results: ")
//        TestUtils.printShifts(autoFilledShifts)
        for(s <- autoFilledShifts) {
          println(s._1)
//          println(s)
        }
        println("\n")

        val unassignedShifts = autoFilledShifts.filter(s => s._2.isEmpty)
        println("unassigned shifts: ".concat(unassignedShifts.size.toString))

        val assigned = autoFilledShifts.filterNot(unassignedShifts.contains(_))
        println("assigned shifts: ".concat(assigned.size.toString()))

        val out = ShiftManager.resolveUnassigned(families, unassignedShifts.map(_._1), assigned, List[Shift](), Map((Shift.TYPES.ORGANISE -> 1.5), (Shift.TYPES.GUARD -> 8.0)))

        println("\n\n1st pass resolve unassigned results: ")
        TestUtils.printShifts(out._1.map(s => (s, None)) ::: out._2)
        println("\n")
        println("assigned: ".concat(out._2.size.toString()))
        println("unassigned: ".concat(out._1.size.toString()))
        out
      }
      case None => {
        val autoFilledShifts = greedyAutoFill(shifts, families, limits)
        val unassignedShifts = autoFilledShifts.filter(s => s._2.isEmpty)
        val assigned = autoFilledShifts.filterNot(unassignedShifts.contains(_))
        ShiftManager.resolveUnassigned(families, unassignedShifts.map(_._1), assigned, List[Shift](), Map((Shift.TYPES.ORGANISE -> 1.5), (Shift.TYPES.GUARD -> 8.0)))
      }
    }
  }

  /**
    * Tries to assigns Family to Shifts
    *
    * @param shifts List of Shift to be assigned
    * @param families List of Family to be assigned
    * @param limits
    * @return ._1 is an shift and ._2 is Some(Family) or None if a shift is was not assigned
    */
  def greedyAutoFill(shifts:List[Shift], families:List[Family], limits:Map[String, Int]) : List[(Shift, Option[Family])] = {

    if(shifts.map(s => s.date.get(Calendar.WEEK_OF_YEAR)).toSet.size != 1) {
      throw new IllegalStateException("Exception thrown")
    }
    val weekNumber = shifts.map(s => s.date.get(Calendar.WEEK_OF_YEAR)).head


    var i = 0
    for(s <- shifts) yield {
      i += 1
      //find first family with no shifts for the week
      val contenders = families.filter(f => f.shifts.isEmpty)
//      println("\nContenders (loop " + i + ") - Looking for families without any shifts:")
//      for(c <- contenders) println(c)

      if(contenders.nonEmpty) {
//        println("Contender found ( " + contenders.head.id + " )")
        contenders.head.addShift(s)
//        println("Shift added: " + contenders.head)
        (s, Some(contenders.head))
      }
      else {
//        println("No contender found - all families have at least 1 shift")
        // get SHIFT type
        // find first family with NO shifts of that type for the week && no shifts that day
//        println("Looking for families without shifts of type " + s.shiftType.shiftType + ", and date " + s.date.getTime.toString)

        val filteredResult = s.shiftType match {
          case ShiftType(_, Shift.TYPES.ORGANISE, _) => {
            families.filter(f => f.getShiftsByWeek(weekNumber, s).size == 0 && f.hasAShiftOnDay(s.date) == false)
          }
          case ShiftType(_, Shift.TYPES.GUARD, _) => {
            families.filter(f => f.getShiftsByWeek(weekNumber, s).size < limits.getOrElse(Shift.TYPES.GUARD, 0) && f.hasAShiftOnDay(s.date) == false)
          }
        }

        if(filteredResult.nonEmpty) {
//          println("Contender (already with shifts) found ( " + filteredResult.head.id + " )")
          filteredResult.head.addShift(s)
          (s, Some(filteredResult.head))
        }
        else {
//          println("No contender (already with shifts) found - all families have at least 1 shift")
          s.shiftType match {
            // if SHIFT.TYPE is OPEN_CLOSE (threshold is 1 shift per week per child)
            // FLAG for REQUEST_EXTRA
            case ShiftType(_, Shift.TYPES.ORGANISE, _) =>
              (s, None)
            // find first family with only 1 GUARD
            case ShiftType(_, Shift.TYPES.GUARD, _) =>
              val pickedFamily = removeByDate(families, s.date).filter(f => f.shifts.toList.count(sh => sh.shiftType.shiftType == s.shiftType.shiftType) < limits.getOrElse(Shift.TYPES.GUARD, 0))
              if(pickedFamily.nonEmpty) {
                pickedFamily.head.addShift(s)
                (s, Some(pickedFamily.head))
              }
              else {
                (s, None)
              }
          }
        }
      }
    }
  }

  /**
    * Sums the duration of shifts by type and checks against max durations for each shift type
    * @param shifts List of shifts to sum
    * @param limits Map of max durations Key: shiftType, Value: Max duration
    * @return true if limits not exceeded, otherwise false
    */
  def isWithinMaxShiftDurations(shifts:List[Shift], limits:Map[String, Double]):Boolean = {
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

  def filterShiftsByDates(dates:List[Calendar], shifts:List[Shift]) = shifts
      .filter(s => {
        dates.filter(d => DateUtils.sameDay(s.date, d)).isEmpty
      })


  /**
    * Recursive function that tries to resolve the 'unassigned' shifts parameter.
    *
    * 1. It searches the list of assigned shifts and checks the families' shifts to see if it can be assigned to the family give a 'criteria'
    * TODO: optimise checking families
    * TODO: add to criteria - no same date
    *
    * @param unassigned Input - list of unassigned shifts
    * @param assigned Input/Output - list of tuples of assigned shifts (Key) and family (Value)
    * @param unresolved Output - list of shifts that where assigning a family has been unsuccessful
    * @param limits Map of max durations Key: shiftType, Value: Max duration
    * @return a tuple containing unresolved shifts (_._1) and shifts with families (_._2)
    */
  def resolveUnassigned(families:List[Family], unassigned:List[Shift], assigned:List[(Shift, Option[Family])], unresolved:List[Shift], limits:Map[String, Double]):(List[Shift], List[(Shift, Option[Family])]) = {



    /**
      * first pass - simple pass to fix or be implemented into autoFill
      * for unassigned shift-i
      * find families with space on their agenda
      * try to assign shift, otherwise store as unresolved
      *
      * also need list of families with space
      *
      * second pass - create list of swappable - TODO
      * for unresolved shift-i
      * find all shifts of same type
      * try
      */

    //get all unassigned shifts
    println("Resolving ".concat(unassigned.size.toString).concat(" unassigned shifts"))
    unassigned match {
      case head :: tail => {
        println("======= head :: tail - " + tail.size)
        println("shift - " + head)
        val shiftType = head.shiftType.shiftType

        // filter assigned shifts by shift type and whose family has not exceed duration for that shift
        val filteredAndSortedAssigned = assigned.filter(s => s._1.shiftType.shiftType == shiftType)
          .filter(f => {
            f._2.get.shifts.filter(s => s.shiftType.shiftType == shiftType)
              .map(_.shiftType.duration).sum <= limits.getOrElse(shiftType, 0.0)
          })
          .sortBy(_._2.get.shifts.filter(s => s.shiftType.shiftType == shiftType)
            .map(_.shiftType.duration).sum)

        var a = assigned

        breakable {
          for(s <- filteredAndSortedAssigned) {
            // can family add shift?
            val f = s._2.get.shifts.toList ::: List(head)
            if(isWithinMaxShiftDurations(f, limits) == true) {
              if(!s._2.get.hasAShiftOnDay(head.date)) {
                s._2.get.addShift(head)
                a = a :+ (head, s._2)
                break
              } else {
                // TODO - find families that can swap
                // Swap = a family with no shift on same day as s && shift of same type
                println("HEAD: ", head)
                //family with least shifts - ie. missing shifts
                val c = families
                  .sortBy(_.getShiftsByType(head.date.get(Calendar.WEEK_OF_YEAR), head).size)
                  .head
                println("c: ", c)

                //all shifts on different days to c shifts
                val cDates = c.shifts.map(s => s.date)
                val possibleSwaps = families
                  .filter(f => f != c)
                  .map(f => f.getShiftsByType(head.date.get(Calendar.WEEK_OF_YEAR), head))
                  .map(shifts => filterShiftsByDates(cDates.toList, shifts))
                  .flatten
                possibleSwaps.foreach(d => println("possibleSwap", d))
                possibleSwaps.headOption match {
                  case Some(s) => {
                    val family = s.family.get

                    // remove shift to swap

                    println("Removing ".concat(s.toString()).concat(" from ").concat(family.toString()))

                    family.removeShift(s)

                    println(family.id.concat(" current status: ").concat(family.shifts.toString()))

                    a = a.filter(j => j._1 != s)

                    // add unassigned
                    family.addShift(head)
                    a = a :+ (head, Option(family))



                    // copy then bin s
                    c.addShift(s)
                    a = a :+ (head, Option(c))
                    println(c.id.concat(" current status: ").concat(c.shifts.toString()))
//                    val n = Shift(s.id, s.shiftType, s.date, Option(c))
                    // place s in c

                  }
                  case None =>
                }
              }
            }




          }
        }
        val resolved = a

        if(resolved.size > assigned.size) {
          resolveUnassigned(families, tail, resolved, unresolved, limits)
        } else {
          resolveUnassigned(families, tail, assigned, unresolved ::: List(head), limits)
        }
      }
      case Nil => {
        //          println("======= Nil")
        (unresolved, assigned)
      }
    }
  }
}
