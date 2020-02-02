package model2

import java.util.Calendar

import model.Identifiable
import model2.shifts.ScheduledShift
import utils.DateUtils

import scala.collection.mutable.{HashMap, ListBuffer}


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
  private val mappedShifts: HashMap[Int, ListBuffer[ScheduledShift]] = HashMap[Int, ListBuffer[ScheduledShift]]()

  def addShift(shift:ScheduledShift):Family = {

    mappedShifts.get(shift.date.get(Calendar.WEEK_OF_YEAR)) match {
      case Some(r) => r += shift
      case None => mappedShifts.put(shift.date.get(Calendar.WEEK_OF_YEAR), ListBuffer(shift))
    }
    shift.family = Option(this)


    if(shifts.size != mappedShifts.flatten(i => i._2).size) {
      throw new Exception("Shifts not synced")
    }

    this
  }

  def removeShift(shift:ScheduledShift) = {
    mappedShifts.get(shift.date.get(Calendar.WEEK_OF_YEAR)) match {
      case Some(r) => {
        println("mappedShifts Before", r)
        r -= (shift)
        println("mappedShifts After", r)
        r.toList
      }
      case None => List[ScheduledShift]()
    }
  }

  /**
    * TODO rename to getShiftByCategory, and remember this processes only one week
    * @param week
    * @param shift
    * @return
    */
  def getShiftsByType(week:Int, shift:ScheduledShift):List[ScheduledShift] = getShiftsByWeek(week, shift)

//  def getShiftsByWeek(week:Int):List[ScheduledShift] = {
//    mappedShifts.get(week) match {
//      case Some(r) => r.toList
//      case None => List[ScheduledShift]()
//    }
//  }

  def getShiftsByWeek(week:Int, shift:ScheduledShift):List[ScheduledShift] = {
    mappedShifts.get(week) match {
      case Some(r) => r.toList.filter(s => s.definition.category == shift.definition.category)
      case None => List[ScheduledShift]()
    }
  }

  def getShiftsByWeek(week:Int, shiftCategory:String):List[ScheduledShift] = {
    mappedShifts.get(week) match {
      case Some(r) => r.toList.filter(s => s.definition.category == shiftCategory)
      case None => List[ScheduledShift]()
    }
  }

  def numShiftsOnDay(date:Calendar):Int = {
    val list = this.shifts.filter(s => DateUtils.sameDay(s.date, date))
    list match {
      case Nil => 0
      case head :: Nil => 1
      case head :: tail => list.size
    }
  }

  def hasAShiftOnDay(date:Calendar):Boolean =
    this.shifts.find(s => DateUtils.sameDay(s.date, date)) match {
      case Some(i) => true
      case None => false
    }

  override def toString() : String = {
    val s = shifts.map(i => i.id.concat(" " + i.date.getTime.toString))
    "[Family: " + id + ", Shifts : " + s
  }
}
