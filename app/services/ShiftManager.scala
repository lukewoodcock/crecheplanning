package services

import java.util.Calendar

import model.{Family, Shift, ShiftType}
import utils.DateUtils

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

  def autoFill(shifts:List[Shift], families:List[Family], limits:Map[String, Int]) : List[(Shift, Option[Family])] = {
    for(s <- shifts) yield {
      // remove unavailable families
      val contenders = families.toList
      //        .filter(f =>
      //          f.noCanDo.find(nS => nS.id == s.id).nonEmpty
      //        )


      //find first family with no shifts for the week
      val toto = contenders.filter(f => f.shifts.isEmpty)
      if(toto.nonEmpty) {
        toto.head.addShift(s)
        (s, Some(toto.head))
      }
      else {
        // get SHIFT type
        // find first family with NO shifts of that type for the week && no shifts that day
        val tata2 = removeByShiftId(
          removeByDate(contenders, s.date)
          , s.shiftType.id)
        val tata = s.shiftType match {
          case ShiftType(_, Shift.TYPES.ORGANISE, _) => tata2.filter(f => f.shifts.toList.count(sh => sh.shiftType.shiftType == s.shiftType.shiftType) < limits.getOrElse(Shift.TYPES.ORGANISE, 0))
          case ShiftType(_, Shift.TYPES.GUARD, _) => tata2.filter(f => f.shifts.toList.count(sh => sh.shiftType.shiftType == s.shiftType.shiftType) < limits.getOrElse(Shift.TYPES.GUARD, 0))
        }

        if(tata.nonEmpty) {
          tata.head.addShift(s)
          (s, Some(tata.head))
        }
        else {
          s.shiftType match {
            // if SHIFT.TYPE is OPEN_CLOSE (threshold is 1 shift per week per child)
            // FLAG for REQUEST_EXTRA
            case ShiftType(_, Shift.TYPES.ORGANISE, _) =>
              (s, None)
            // find first family with only 1 GUARD
            case ShiftType(_, Shift.TYPES.GUARD, _) =>
              val titi = removeByDate(contenders, s.date).filter(f => f.shifts.toList.count(sh => sh.shiftType.shiftType == s.shiftType.shiftType) < limits.getOrElse(Shift.TYPES.GUARD, 0))
              if(titi.nonEmpty) {
                titi.head.addShift(s)
                (s, Some(titi.head))
              }
              else (s, None)
          }
        }
      }

      //      //find first family with no shifts for the week
      //      contenders.filter(f =>
      //        f.shifts.isEmpty
      //      ) match {
      //        case head :: tail => {
      //          head.shifts += s
      //          (s, Some(head))
      //        }
      //        case head :: Nil => {
      //          head.shifts += s
      //          (s, Some(head))
      //        }
      //        case Nil => {
      //          // get SHIFT type
      //          // find first family with NO shifts of that type for the week && no shifts that dayx
      //          contenders.filter(f =>
      //            f.shifts
      //              .filter(fs => fs.date != s.date)
      //              .filter(fs => fs.shiftType != s.shiftType)
      //              .isEmpty
      //          ) match {
      //            case head :: tail => {
      //              head.shifts += s
      //              (s, Some(head))
      //            }
      //            case head :: Nil => {
      //              head.shifts += s
      //              (s, Some(head))
      //            }
      //            case Nil => {
      //              s.shiftType match {
      //                // if SHIFT.TYPE is OPEN_CLOSE (threshold is 1 shift per week per child)
      //                // FLAG for REQUEST_EXTRA
      //                case ShiftType(Shift.OPENING, _) | ShiftType(Shift.CLOSING, _) => (s, None)
      //                // find first family with only 1 GUARD
      //                case _ => {
      //                  contenders.filter(f =>
      //                    f.shifts.filter(fs =>
      //                      fs.shiftType match {
      //                        case ShiftType(Shift.OPENING, _) | ShiftType(Shift.CLOSING, _) => false
      //                        case _ => true
      //                      }
      //                    )
      //                    .size <= 1
      //                  ) match {
      //                    case head :: tail => {
      //                      head.shifts += s
      //                      (s, Some(head))
      //                    }
      //                    case head :: Nil => {
      //                      head.shifts += s
      //                      (s, Some(head))
      //                    }
      //                    case Nil => (s, None)
      //                  }
      //                }
      //              }
      //            }
      //          }
      //        }
      //      }
    }
  }

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
    * @param unassigned Input - list of unassigned shifts
    * @param assigned Input/Output - list of tuples of assigned shifts (Key) and family (Value)
    * @param unresolved Output - list of shifts that where assigning a family has been unsuccessful
    * @param limits Map of max durations Key: shiftType, Value: Max duration
    * @return a tuple containing unresolved shifts (_._1) and shifts with families (_._2)
    */
  def resolveUnassigned(unassigned:List[Shift], assigned:List[(Shift, Option[Family])], unresolved:List[Shift], limits:Map[String, Double]):(List[Shift], List[(Shift, Option[Family])]) = {



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
    unassigned match {
      case head :: tail => {
        //          println("======= head :: tail - " + tail.size)
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
            if(isUnderMaxShiftDurations(f, limits) == true) {
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
          resolveUnassigned(tail, resolved, unresolved, limits)
        } else {
          resolveUnassigned(tail, assigned, unresolved ::: List(head), limits)
        }
      }
      case Nil => {
        //          println("======= Nil")
        (unresolved, assigned)
      }
    }
  }
}
