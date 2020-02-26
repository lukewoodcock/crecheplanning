package model

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import model.{Family, Identifiable}

/**
  * {
  * "id": "OPEN",
  * "description": "Open crèche",
  * "startTime": "08:00:00",
  * "endTime": "09:30:00",
  * "skillsRequirements": [
  * "Organise"
  * ]
  * }
  */

case class ShiftDefinition(override val id: String, category: String, description: String, startTime: String, endTime: String, skillsRequirements:List[String]) extends Identifiable[String](id)

object ScheduledShiftResult {
  def fromScheduledShift(s:ScheduledShift) = {
    ScheduledShiftResult(s.definition.id, s.start.toString, s.end.toString, s.family match {
      case Some(value) => value.id
      case None => "Undefined"}
    )
  }
}

case class ScheduledShiftResult(override val id: String, val start: String, val end: String, val family: String) extends Identifiable[String](id)

object ScheduledShift {
  def getWeek(shift:ScheduledShift) = shift.date.get(Calendar.YEAR).toString.concat("_").concat(shift.date.get(Calendar.WEEK_OF_YEAR).toString)
}
case class ScheduledShift(override val id: String, definition: ShiftDefinition, date:Calendar, var family: Option[Family]) extends Identifiable[String](id){

  val DATE_FORMAT = "yyyy-mm-dd hh:mm:ss"

  private def convertStringToDate(s: String): Date = {
    val dateFormat = new SimpleDateFormat(DATE_FORMAT)
    dateFormat.parse(s)
  }

  def start: Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    .parse(new SimpleDateFormat("yyyy-MM-dd")
      .format(date.getTime)
      .concat(" ")
      .concat(definition.startTime))

  def end: Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    .parse(new SimpleDateFormat("yyyy-MM-dd")
      .format(date.getTime)
      .concat(" ")
      .concat(definition.endTime))

  def duration: Long = Math.abs(end.getTime() - start.getTime())

  override def toString() : String = {
    family match {
      case Some(value) => "Shift: " + definition.id + ", Family: " + value.id + " - " + value.contractId + ", Date : " + this.start.toString
      case None => "Shift: " + definition.id + ", Family: None, Date : " + this.start.toString
    }
  }
}
