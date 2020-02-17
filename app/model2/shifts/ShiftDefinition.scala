package model2.shifts

import java.text.{DateFormat, SimpleDateFormat}
import java.util.{Calendar, Date}

import model.Identifiable
import model2.Family

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


object ScheduledShift {
  def getWeek(shift:ScheduledShift) = shift.date.get(Calendar.YEAR).toString.concat("_").concat(shift.date.get(Calendar.WEEK_OF_YEAR).toString)
}
case class ScheduledShift(override val id: String, definition: ShiftDefinition, date:Calendar, var family: Option[Family]) extends Identifiable[String](id){

  def start: Date =  new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
    .parse(new SimpleDateFormat("yyyy-mm-dd")
      .format(date.getTime)
      .concat(" ")
      .concat(definition.startTime)
    )

  def end: Date = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
    .parse(new SimpleDateFormat("yyyy-mm-dd")
        .format(date.getTime)
        .concat(" ")
        .concat(definition.endTime)
    )

  def duration: Long = Math.abs(end.getTime() - start.getTime())

  override def toString() : String = {
    family match {
      case Some(value) => "Shift: " + definition.id + ", Family: " + value.id + " - " + value.contractId + ", Date : " + this.start.toString
      case None => "Shift: " + definition.id + ", Family: None, Date : " + this.start.toString
    }
  }

//  override def toString() : String = {
//    family match {
//      case Some(f) => {
//        "Shift: " + definition.id + ", family: " + f.id + ", date: " + date.getTime.toString
//      }
//      case None => "ShiftDefintion: No family"
//    }
//  }
}
