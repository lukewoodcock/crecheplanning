package model2

import model.Identifiable

/**
  * {
  * "id": 0,
  * "description": "standard_moyens",
  * "shiftRules": [
  * {
  * "shiftTypes": [
  * "OPEN",
  * "CLOSE"
  * ],
  * "limits": {
  * "daily": 1,
  * "weekly": 1
  * }
  * },
  * {
  * "shiftTypes": [
  * "MORNING_MOYENS",
  * "AFTERNOON_MOYENS",
  * "GYM_MOYENS"
  * ],
  * "limits": {
  * "daily": 1,
  * "weekly": 2
  * }
  * }
  * ]
  * }
  */

case class Limits(daily:Option[Int], weekly:Option[Int], monthly:Option[Int]) {
  def toList = {
//    if(!daily.isDefined && !weekly.isDefined && !monthly.isDefined) {
//      List.empty()
//    }
    List(daily, weekly, monthly)
  }
}

case class ShiftRule(shiftDefinitionIds:List[String], limits:Limits)

case class Contract(override val id: Int, globalLimits: Limits, shiftRules:List[ShiftRule]) extends Identifiable[Int](id)
