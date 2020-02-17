package model2

import java.util.Calendar

import model.Identifiable
import model2.shifts.ScheduledShift
import utils.DateUtils

import scala.collection.mutable.{HashMap, ListBuffer}


//trait Contracted {
////  def assignContracts(families:Array[Family], contracts:Array[Contract]) = families.foreach(f => contracts)
//  var contract:Int
//}

object Family {
  def hasSkill(family: Family, skill:String) = family.skills.contains(skill)
  def hasSkills(family: Family, skills:List[String]) = skills.forall(s => family.skills.contains(s))
}

/**
  * {
  * "id": 0,
  * "contractId": 0,
  * "name": "EMMA",
  * "skills": [
  * "Organise",
  * "Guard_Moyens",
  * "Gym_Moyens"
  * ]
  * }
  */

case class Family(override val id: String, contractId: Int, name: String, skills:List[String]) extends Identifiable[String](id) {

  var contract: Option[Contract] = None

  def shifts = mappedShifts
    .values
    .flatten
    .toList

  // todo refactor key to be unique... weeks of year repeat each year
  /**
    * HashMap of shift where key is the week of the year
    *
    *  @tparam A    key is an Int representing the week of the year
    *  @tparam B    value is ListBuffer of Shift
    */
  private val mappedShifts: HashMap[String, ListBuffer[ScheduledShift]] = HashMap[String, ListBuffer[ScheduledShift]]()

  def addShift(shift:ScheduledShift):Family = {

    mappedShifts.get(ScheduledShift.getWeek(shift)) match {
      case Some(r) => r += shift
      case None => mappedShifts.put(ScheduledShift.getWeek(shift), ListBuffer(shift))
    }
    shift.family = Option(this)


    if(shifts.size != mappedShifts.flatten(i => i._2).size) {
      throw new Exception("Shifts not synced")
    }

    this
  }

  def removeShift(shift:ScheduledShift) = {
    mappedShifts.get(ScheduledShift.getWeek(shift)) match {
      case Some(r) => {
        println("mappedShifts Before", r)
        r -= (shift)
        println("mappedShifts After", r)
        r.toList
      }
      case None => List[ScheduledShift]()
    }
  }

  def getShiftsByCategoryForWeek(week:String, shift:ScheduledShift):List[ScheduledShift] = {
    mappedShifts.get(week) match {
      case Some(r) => r.toList.filter(s => s.definition.category == shift.definition.category) //TODO use shift id with contract
      case None => List[ScheduledShift]()
    }
  }

  def getShiftsByCategoryForWeek(week:String, shiftCategory:String):List[ScheduledShift] = {
    mappedShifts.get(week) match {
      case Some(r) => r.toList.filter(s => s.definition.category == shiftCategory)
      case None => List[ScheduledShift]()
    }
  }

  def numShiftsOnDay(date:Calendar):Int = this.shifts.filter(s => DateUtils.sameDay(s.date, date)).size

  def hasAShiftOnDay(date:Calendar):Boolean = numShiftsOnDay(date) > 0

  override def toString() : String = {
    val s = shifts.map(i => i.id.concat(" " + i.date.getTime.toString))
    "[Family: " + id + ", Shifts : " + s
  }
}
