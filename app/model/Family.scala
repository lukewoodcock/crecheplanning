package model

import java.util.Calendar

import utils.DateUtils

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

object Family {

}

case class Family(override val id: String) extends Identifiable[String](id) {
  def shifts = mappedShifts
    .values
    .flatten
    .toList

  val noCanDo: ListBuffer[Shift] = ListBuffer[Shift]()

  // todo refactor key to be unique... weeks of year repeat each year
  /**
    * HashMap of shift where key is the week of the year
    *
    *  @tparam A    key is an Int representing the week of the year
    *  @tparam B    value is ListBuffer of Shift
    */
  var mappedShifts: HashMap[Int, ListBuffer[Shift]] = HashMap[Int, ListBuffer[Shift]]()


  def hasOrganise(n:Int):Boolean = shifts.filter(s => s.shiftType.shiftType.equals(Shift.TYPES.ORGANISE)).toList.size >= n
  def hasGuard(n:Int):Boolean = shifts.filter(s => s.shiftType.shiftType.equals(Shift.TYPES.GUARD)).toList.size >= n

  def addShift(shift:Shift):Family = {

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

  def removeShift(shift:Shift) = {



    mappedShifts.get(shift.date.get(Calendar.WEEK_OF_YEAR)) match {
      case Some(r) => {
        println("mappedShifts Before", r)
        r -= (shift)
        println("mappedShifts After", r)
      }
      r.toList
    }
  }

  def getShiftsByType(week:Int, shift:Shift):List[Shift] = getShiftsByWeek(week, shift)

  def getShiftsByWeek(week:Int):List[Shift] = {
    mappedShifts.get(week) match {
      case Some(r) => r.toList
      case None => List[Shift]()
    }
  }

  def getShiftsByWeek(week:Int, shift:Shift):List[Shift] = {
    mappedShifts.get(week) match {
      case Some(r) => r.toList.filter(s => s.shiftType.shiftType == shift.shiftType.shiftType)
      case None => List[Shift]()
    }
  }

  def hasAShiftOnDay(date:Calendar):Boolean =
    this.shifts.find(s => DateUtils.sameDay(s.date, date)) match {
      case Some(i) => true
      case None => false
    }

  override def toString() : String = {

//    val s = shifts.toList.map(i => i.id.concat(i.date.getTime.toString))
    val s = shifts.toList.map(i => i.id.concat(" " + i.date.getTime.toString))
//    for(i <- shifts.toList.map(s => s.id.concat(s.date.getTime.toString))) s += "\n"

    return "[Family: " + id +
      ", Shifts : " + s
  }
}