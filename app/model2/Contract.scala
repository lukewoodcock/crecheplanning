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

case class Limits(daily:Option[Int], weekly:Option[Int], monthly:Option[Int])

case class ShiftRule(shiftTypes:List[String], limits:Limits)

case class Contract(override val id: Int, shiftRules:List[ShiftRule]) extends Identifiable[Int](id)