package services

import java.util.Calendar

import model.{Contract, Family, Limits, ScheduledShift}
import utils.ShiftUtils

import scala.util.control.Breaks.{break, breakable}

object ShiftManager {

  /***
    * Given a list of Family, list of Contract and a list of ScheduledShift (grouped by week), try to assign a Family to each ScheduledShift
    * @param families
    * @param weeksToResolve
    * @param contracts
    * @return List[(ScheduledShift, Option[Family])]
    */
  def resolve(families:List[Family], weeksToResolve:List[(String, List[ScheduledShift])], contracts:List[Contract]): List[(ScheduledShift, Option[Family])] = {
    var _families = families
    var results:(List[ScheduledShift], List[(ScheduledShift, Option[Family])]) = (List[ScheduledShift](), List[(ScheduledShift, Option[Family])]())
    for(week <- weeksToResolve) {
      val it = ShiftManager.autoFillWeek(week._2
      , _families
      , contracts
      )
      results = (results._1 ::: it._1, results._2 ::: it._2)
      val af = _families.sortBy(_.shifts.size)
      _families = af
    }
    results._2
  }

  /***
    * Given a list of Family, list of Contract and a list of ScheduledShift (grouped by week), try to assign a Family to each ScheduledShift
    * @param shifts
    * @param families
    * @param contracts
    * @param debug
    * @return
    */
  def autoFillWeek(shifts:List[ScheduledShift], families:List[Family], contracts:List[Contract], debug:Option[String] = None):(List[ScheduledShift], List[(ScheduledShift, Option[Family])])  = {
    debug match {
      case Some(debugString) => {
        val autoFilledShifts = greedyAutoFill(shifts, families, contracts)
        val unassignedShifts = autoFilledShifts.filter(shift => shift._2.isEmpty)
        val assigned = autoFilledShifts.filterNot(unassignedShifts.contains(_))
        val out = ShiftManager.resolveUnassigned(families, unassignedShifts.map(_._1), assigned, List[ScheduledShift](), contracts)
        out
      }
      case None => {
        val autoFilledShifts = greedyAutoFill(shifts, families, contracts)
        val unassignedShifts = autoFilledShifts.filter(shift => shift._2.isEmpty)
        val toto = autoFilledShifts.map(s => s._1.definition.id)
        val assigned = autoFilledShifts.filterNot(unassignedShifts.contains(_))
        val out = ShiftManager.resolveUnassigned(families, unassignedShifts.map(_._1), assigned, List[ScheduledShift](), contracts)
        out
      }
    }
  }

  /***
    * Greedily assign Family to list of ScheduledShift
    * @param shifts
    * @param families
    * @param contracts
    * @return
    */
  def greedyAutoFill(shifts:List[ScheduledShift], families:List[Family], contracts:List[Contract]) : List[(ScheduledShift, Option[Family])] = {
    if(shifts.map(s => s.date.get(Calendar.WEEK_OF_YEAR)).toSet.size != 1) {
      throw new IllegalStateException("Exception thrown")
    }
    val weekNumber = shifts.map(s => s.date.get(Calendar.WEEK_OF_YEAR)).head
    for(s <- shifts) yield {

      //find first family with no shifts for the week
      //TODO until API handles monthly limits we assume contract not exceeded
      if(families.filter(f => f.shifts.isEmpty).nonEmpty) {
//        println("NO SHIFTS : find first family with no shifts for the week and has all skills. " )
        val contenders = families
          .filter(f => f.shifts.isEmpty)
          .filter(f => f.isAbsent(s) == false)
          .filter(f => Family.hasSkills(f, s.definition.skillsRequirements))

        val out = contenders.headOption match {
          case Some(contender) => {
//            println("NO SHIFTS, HAS SKILLS : Family " + contender.id + " found and shift added")
            contender.addShift(s)
            (s, Some(contender))
          }
          case None => {
//            println("NON SHIFTS, NO SKILLS : No family found")
            //TODO extract **.**
            val filteredResult = families
              .filter(f => {
                f.isAbsent(s) == false
              })
              .filter(f => Family.hasSkills(f, s.definition.skillsRequirements))
              .filter(f => contracts.find(c => c.id == f.contractId).isDefined)
              .filter(f => {
                val contract = contracts.find(c => c.id == f.contractId).get

                //check globals
                !exceedLimit(f.shifts, contract.globalLimits, s.date, s.definition.category)
              })
              .filter(f => {
                val contract = contracts.find(c => c.id == f.contractId).get
                contract.shiftRules.find(sr => sr.shiftDefinitionIds.contains(s.definition.id)) match {
                  case Some(rule) => !exceedLimit(f.shifts, rule.limits, s.date, s.definition.category)
                  case _ => false
                }
              })
              .sortWith(_.getShiftsByCategoryForWeek(ScheduledShift.getWeek(s), s).size < _.getShiftsByCategoryForWeek(ScheduledShift.getWeek(s), s).size)

            filteredResult.headOption match {
              case Some(family) => {
                family.addShift(s)
//                println("Found a family who has not exceeded either global or shift rule limits")
              }
            }
            (s, filteredResult.headOption)
          }
        }
        out
      } else {
//        println("All families have at least 1 shift")
//        println("find family whose contract is not exceeded")
        //TODO extract **.**
        val filteredResult = families
          .filter(f => {
            f.isAbsent(s) == false
          })
          .filter(f => Family.hasSkills(f, s.definition.skillsRequirements))
          .filter(f => contracts.find(c => c.id == f.contractId).isDefined)
          .filter(f => {
            val contract = contracts.find(c => c.id == f.contractId).get

            //check globals
            !exceedLimit(f.shifts, contract.globalLimits, s.date, s.definition.category)
          })
          .filter(f => {
            val contract = contracts.find(c => c.id == f.contractId).get
            contract.shiftRules.find(sr => sr.shiftDefinitionIds.contains(s.definition.id)) match {
              case Some(rule) => !exceedLimit(f.shifts, rule.limits, s.date, s.definition.category)
              case _ => false
            }
          })
          .sortWith(_.getShiftsByCategoryForWeek(ScheduledShift.getWeek(s), s).size < _.getShiftsByCategoryForWeek(ScheduledShift.getWeek(s), s).size)

        filteredResult.headOption match {
          case Some(family) => {
            family.addShift(s)
//            println("Found a family who has not exceeded either global or shift rule limits")
            (s, Some(family))
          }
          case None => (s, None)
        }
      }
    }
  }

  @deprecated("this method has been replace by def exceedLimit(shifts: List[ScheduledShift], limits: Limits, date: Calendar, category: String, op: (Int, Int) => Boolean)")
  def exceedLimit(shifts: List[ScheduledShift], limits: Limits, date: Calendar, category: String):Boolean = {
    limits.daily match {
      case Some(value) => {
        if(ShiftUtils.numShiftsOnDay(shifts, date) >= value) return true
      }
      case _ =>
    }

    limits.weekly match {
      case Some(value) => {
        val week =
        if(ShiftUtils.getShiftsByCategoryForWeek(shifts, category, date.get(Calendar.WEEK_OF_YEAR)).size >= value) return true
      }
      case _ =>
    }

    false
  }

  /***
    * Checks to see if a list of ScheduledShift exceeds limitations defined by Limits and a given compare operation
    *
    * TODO remove category and refactor Limits mechanism to require defintions for all ShiftDefinition::id
    *
    * @param shifts
    * @param limits
    * @param date
    * @param category
    * @param op compare operation e.g. val lessOrEqual = (a: Int, b: Int) => a <= b
    * @return true if list exceeds limitations
    */
  def exceedLimit(shifts: List[ScheduledShift], limits: Limits, date: Calendar, category: String, op: (Int, Int) => Boolean):Boolean = {
    limits.daily match {
      case Some(value) => {
        if(op(ShiftUtils.numShiftsOnDay(shifts, date), value)) return true
      }
      case _ =>
    }

    limits.weekly match {
      case Some(value) => {
        val week =
          if(op(ShiftUtils.getShiftsByCategoryForWeek(shifts, category, date.get(Calendar.WEEK_OF_YEAR)).size, value)) return true
      }
      case _ =>
    }

    false
  }

  /***
    * This algorithm tries to assign Families to ScheduledShift without an assigned Family by swapping shifts
    *
    * Note: a solution is not guaranteed.
    * TODO add option to guard original ScheduledShift
    *
    * @param families
    * @param unassigned
    * @param assigned
    * @param unresolved
    * @param contracts
    * @return
    */
  def resolveUnassigned(families:List[Family], unassigned:List[ScheduledShift], assigned:List[(ScheduledShift, Option[Family])], unresolved:List[ScheduledShift], contracts:List[Contract]):(List[ScheduledShift], List[(ScheduledShift, Option[Family])]) = {
    unassigned match {
      case head :: tail => {

        val lessOrEqual = (a: Int, b: Int) => a <= b

        // remove scheduled shifts whose family already meets quota
        // filter assigned shifts by id (shift type) and whose family has not exceed duration for that shift as per their contract
        val filteredAndSortedAssigned_toto = assigned
//          .filter(s => s._1.definition.id == head.definition.id)
          .filter(s => s._1.definition.category.equalsIgnoreCase(head.definition.category))
          .filter(s => {
            val family = s._2.get
            val familyContract = contracts.find(contract => contract.id == family.contractId).get
            val shiftRule = familyContract.shiftRules.find(rules => rules.shiftDefinitionIds.contains(s._1.definition.id)).headOption.get
            exceedLimit(family.shifts, familyContract.globalLimits, s._1.date, s._1.definition.category, lessOrEqual) && exceedLimit(family.shifts, shiftRule.limits, s._1.date, s._1.definition.category, lessOrEqual)
          })

        val filteredAndSortedAssigned = filteredAndSortedAssigned_toto
          .sortBy(_._2.get.shifts.filter(shifts => shifts.definition.category == head.definition.category)
                                .map(_.duration)
                                .sum)


        var a = assigned

        breakable {
          for(s <- filteredAndSortedAssigned) {
            //let's see if the family can add shift?
            val shifts = s._2.get.shifts ::: List(head)
            val familyContract = contracts.find(contract => contract.id == s._2.get.contractId).get
            val shiftRule = familyContract.shiftRules.find(rules => rules.shiftDefinitionIds.contains(s._1.definition.id)).headOption.get
            if(exceedLimit(shifts, familyContract.globalLimits.copy(None), s._1.date, s._1.definition.category, lessOrEqual) && exceedLimit(shifts, shiftRule.limits.copy(None), s._1.date, s._1.definition.category, lessOrEqual)) {
              if(!s._2.get.isAbsent(head) && !ShiftUtils.hasShiftOnDay(s._2.get.shifts, head.date)) {
                s._2.get.addShift(head)
                a = a :+ (head, s._2)
                break
              } else {
                val c = families
                  .sortBy(_.getShiftsByCategoryForWeek(ScheduledShift.getWeek(head), head).size)
                  .head

                //all shifts on different days to c shifts
                val cDates = c.shifts.map(s => s.date)
                val possibleSwaps = families
                  .filter(f => f.isAbsent(head) == false)
                  .filter(f => f != c)
                  .map(f => f.getShiftsByCategoryForWeek(ScheduledShift.getWeek(head), head))
                  .map(shifts => ShiftUtils.filterShiftsByDates(cDates, shifts))
                  .flatten

                possibleSwaps.headOption match {
                  case Some(swap) => {
                    val family = swap.family.get
                    family.removeShift(swap)
                    a = a.filter(j => j._1 != swap)
                    family.addShift(head)
                    a = a :+ (head, Option(family))
                    c.addShift(swap)
                    a = a :+ (head, Option(c))
                  }
                  case None =>
                }
              }
            }
          }
        }
        val resolved = a
        if(resolved.size > assigned.size) {
          resolveUnassigned(families, tail, resolved, unresolved, contracts)
        } else {
          resolveUnassigned(families, tail, assigned, unresolved ::: List(head), contracts)
        }
      }
      case  Nil => (unresolved, assigned)
    }
  }
}
