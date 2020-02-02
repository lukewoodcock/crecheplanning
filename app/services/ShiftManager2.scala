package services

import java.util.Calendar

import model2.{Contract, Family, Limits}
import model2.shifts.ScheduledShift

object ShiftManager2 {

  def resolve(_families:List[Family], shiftsToResolve:List[(Int, List[ScheduledShift])], contracts:List[Contract]) = {

    var families = _families

    var results:(List[ScheduledShift], List[(ScheduledShift, Option[Family])]) = (List[ScheduledShift](), List[(ScheduledShift, Option[Family])]())
    for(shift <- shiftsToResolve) {
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
  }

  def autoFillWeek(shifts:List[ScheduledShift], families:List[Family], contracts:List[Contract], debug:Option[String] = None) : List[(ScheduledShift, Option[Family])] = {
    debug match {
      case Some(debugString) => {
        val autoFilledShifts = greedyAutoFill(shifts, families, contracts)
        val unassignedShifts = autoFilledShifts.filter(shift => shift._2.isEmpty)
        val assigned = unassignedShifts.filterNot(unassignedShifts.contains(_))
        assigned //TODO remove
      }
      case None => {
        val autoFilledShifts = greedyAutoFill(shifts, families, contracts)
        val unassignedShifts = autoFilledShifts.filter(shift => shift._2.isEmpty)
        val assigned = unassignedShifts.filterNot(unassignedShifts.contains(_))
        ShiftManager2.resolveUnassigned(families, unassignedShifts.map(_._1), assigned, List[ScheduledShift](), contracts)
        assigned //TODO remove
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
      val contenders = families.filter(f => f.shifts.isEmpty)
      if(contenders.nonEmpty) {
        contenders.head.addShift(s)
        println("STEP 1 : find first family with no shifts for the week. " + contenders.head.id + " found and shift added : " + contenders.head.id)
        (s, Some(contenders.head))
      } else {
        println("All families have at least 1 shift - continue to STEP 2")

        println("STEP 2 : find family whose contract is not exceeded")

        val filteredResult = families.filter(f => contracts.find(c => c.id == f.contractId).isDefined)
          .filter(f => {
            val contract = contracts.find(c => c.id == f.contractId).get

            //check globals
            !exceedLimit(f, contract.globalLimits, s, weekNumber)
          })
          .filter(f => {
            val contract = contracts.find(c => c.id == f.contractId).get
            contract.shiftRules.find(sr => sr.shiftDefinitionIds.contains(s.definition.id)) match {
              case Some(rule) => !exceedLimit(f, rule.limits, s, weekNumber)
              case _ => false
            }
          })
          .sortWith(_.getShiftsByType(weekNumber, s).size < _.getShiftsByType(weekNumber, s).size)

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

  def exceedLimit(family:Family, limits:Limits, shift:ScheduledShift, weekNumber:Int):Boolean = {
    limits.daily match {
      case Some(value) => {
        if(family.numShiftsOnDay(shift.date) >= value) return true
      }
      case _ =>
    }

    limits.weekly match {
      case Some(value) => {
        if(family.getShiftsByWeek(weekNumber, shift).size >= value) return true
      }
      case _ =>
    }

    false
  }

//  def resolveUnassigned(families:List[Family], unassigned:List[ScheduledShift], assigned:List[(ScheduledShift, Option[Family])], unresolved:List[ScheduledShift], limits:Map[String, Double]):(List[ScheduledShift], List[(ScheduledShift, Option[Family])]) = {
  def resolveUnassigned(families:List[Family], unassigned:List[ScheduledShift], assigned:List[(ScheduledShift, Option[Family])], unresolved:List[ScheduledShift], contracts:List[Contract]):(List[ScheduledShift], List[(ScheduledShift, Option[Family])]) = {
    unassigned match {
      case head :: tail => {

        // filter assigned shifts by id and whose family has not exceed duration for that shift as per their contract
        val filteredAndSortedAssigned = assigned.filter(s => s._1.definition.id == head.definition.id)
          .filter(s => {
            val family = s._2.get
            val familyContract = contracts.find(contract => contract.id == family.contractId).get

            val weekNumber  = s._1.date.get(Calendar.WEEK_OF_YEAR)

            //get shift definition ids to filter on
//            val ids = familyContract.shiftRules.filter(rules => rules.shiftDefinitionIds.contains(s._1.definition.id))
//                .map(rules => rules.shiftDefinitionIds)
//              .flatten
//              .toSet

            val shiftRule = familyContract.shiftRules.find(rules => rules.shiftDefinitionIds.contains(s._1.definition.id)).headOption.get

            !exceedLimit(family, familyContract.globalLimits, s._1, weekNumber) && !exceedLimit(family, shiftRule.limits, s._1, weekNumber)
          })
          .sortBy(_._2.get.shifts.filter(shifts => shifts.definition.category == head.definition.category)
                                .map(_.duration)
                                .sum)


        var a = assigned







        (unresolved, assigned) //TODO remove
      }
      case Nil => (unresolved, assigned)
    }
  }
}
