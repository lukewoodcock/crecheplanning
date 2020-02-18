package services

import java.util.Calendar
import java.util.Locale.Category

import model2.{Contract, Family, Limits}
import model2.shifts.ScheduledShift
import utils.ShiftUtils

import scala.util.control.Breaks.{break, breakable}

object ShiftManager2 {

  def resolve(_families:List[Family], weeksToResolve:List[(String, List[ScheduledShift])], contracts:List[Contract]) = {

    var families = _families

//    var results:List[(ScheduledShift, Option[Family])] = List[(ScheduledShift, Option[Family])]()
    var results:(List[ScheduledShift], List[(ScheduledShift, Option[Family])]) = (List[ScheduledShift](), List[(ScheduledShift, Option[Family])]())
    for(week <- weeksToResolve) {

      val it = ShiftManager2.autoFillWeek(week._2
      , families
      , contracts
      )
//      results = results ::: it
      results = (results._1 ::: it._1, results._2 ::: it._2)
      val af = families.sortBy(_.shifts.size)
      families = af
      /**
        * val it = ShiftManager.autoFillWeek(toto._2
        * , families
        * , limits
        * //                  , Option("\n\n================ week ".concat(toto._1.toString).concat("================ "))
        * )
        * results = (results._1 ::: it._1, results._2 ::: it._2)
        * val af = families.sortBy(_.shifts.size)
        * families = af
        */
    }

//    val all = results
//      .filter(r => r._2 match {
//        case Some(fam) => true
//        case None => false
//      })
//      .groupBy(_._2.get.id)
//
//    all
    results._2
  }

//  def autoFillWeek(shifts:List[ScheduledShift], families:List[Family], contracts:List[Contract], debug:Option[String] = None) : List[(ScheduledShift, Option[Family])] = {
  def autoFillWeek(shifts:List[ScheduledShift], families:List[Family], contracts:List[Contract], debug:Option[String] = None):(List[ScheduledShift], List[(ScheduledShift, Option[Family])])  = {
    debug match {
      case Some(debugString) => {
        val autoFilledShifts = greedyAutoFill(shifts, families, contracts)
        val unassignedShifts = autoFilledShifts.filter(shift => shift._2.isEmpty)
        val assigned = autoFilledShifts.filterNot(unassignedShifts.contains(_))
        val out = ShiftManager2.resolveUnassigned(families, unassignedShifts.map(_._1), assigned, List[ScheduledShift](), contracts)
        out //TODO correct output of ShiftManager2.resolveUnassigned and return it
//        assigned //TODO remove
      }
      case None => {
        val autoFilledShifts = greedyAutoFill(shifts, families, contracts)
        val unassignedShifts = autoFilledShifts.filter(shift => shift._2.isEmpty)
        val toto = autoFilledShifts.map(s => s._1.definition.id)
        val assigned = autoFilledShifts.filterNot(unassignedShifts.contains(_))
        val out = ShiftManager2.resolveUnassigned(families, unassignedShifts.map(_._1), assigned, List[ScheduledShift](), contracts)
        out //TODO correct output of ShiftManager2.resolveUnassigned and return it
//        assigned //TODO remove
      }
    }
  }

  def greedyAutoFill(shifts:List[ScheduledShift], families:List[Family], contracts:List[Contract]) : List[(ScheduledShift, Option[Family])] = {
    if(shifts.map(s => s.date.get(Calendar.WEEK_OF_YEAR)).toSet.size != 1) {
      throw new IllegalStateException("Exception thrown")
    }
    val weekNumber = shifts.map(s => s.date.get(Calendar.WEEK_OF_YEAR)).head
    for(s <- shifts) yield {

      //STEP 1 : find first family with no shifts for the week
      //TODO until API handles monthly limits we assume contract not exceeded
      if(families.filter(f => f.shifts.isEmpty).nonEmpty) {
        println("NO SHIFTS : find first family with no shifts for the week and has all skills. " )
        val contenders = families
          .filter(f => f.shifts.isEmpty)
          .filter(f => Family.hasSkills(f, s.definition.skillsRequirements))

        val out = contenders.headOption match {
          case Some(contender) => {
            println("NO SHIFTS, HAS SKILLS : Family " + contender.id + " found and shift added")
            contender.addShift(s)
            (s, Some(contender))
          }
          case None => {
            println("NON SHIFTS, NO SKILLS : No family found")
            val filteredResult = families
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
                println("Found a family who has not exceeded either global or shift rule limits")
//                (s, Some(family))
              }
//              case None => (s, None)
            }
            (s, filteredResult.headOption)
          }
        }
        out
//        (s, contenders.headOption)
      } else {
        println("All families have at least 1 shift - continue to STEP 2")

        println("STEP 2 : find family whose contract is not exceeded")

        val filteredResult = families
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
            println("Found a family who has not exceeded either global or shift rule limits")
            (s, Some(family))
          }
          case None => (s, None)
        }
      }
    }
  }

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

  def resolveUnassigned(families:List[Family], unassigned:List[ScheduledShift], assigned:List[(ScheduledShift, Option[Family])], unresolved:List[ScheduledShift], contracts:List[Contract]):(List[ScheduledShift], List[(ScheduledShift, Option[Family])]) = {
    unassigned match {
      case head :: tail => {

        val lessOrEqual = (a: Int, b: Int) => a <= b
        val two = families.filter(f => f.shifts.size < 3)

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
//             if(!s._2.get.hasAShiftOnDay(head.date)) {
              if(!ShiftUtils.hasShiftOnDay(s._2.get.shifts, head.date)) {
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



//        (unresolved, assigned) //TODO remove
      }
      case  Nil => (unresolved, assigned)
    }
  }

//  private def doesBreachContract(contract: Contract, shifts: List[ScheduledShift], family:Family) = {
//    val familyContract = contracts.find(contract => contract.id == family.contractId).get
//    val weekNumber = s._1.date.get(Calendar.WEEK_OF_YEAR)
//    val shiftRule = familyContract.shiftRules.find(rules => rules.shiftDefinitionIds.contains(s._1.definition.id)).headOption.get
//    !exceedLimit(family, familyContract.globalLimits, s._1, weekNumber) && !exceedLimit(family, shiftRule.limits, s._1, weekNumber)
//  }
}
