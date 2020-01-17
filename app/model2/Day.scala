package model2

import model.Identifiable

/**
  * {
  * "id": "monday",
  * "shifts": [
  * {
  * "shift": "OPEN",
  * "cover": 1
  * },
  * {
  * "shift": "MORNING_MOYENS",
  * "cover": 1
  * },
  * {
  * "shift": "MORNING_GRANDS",
  * "cover": 1
  * },
  * {
  * "shift": "AFTERNOON_MOYENS",
  * "cover": 1
  * },
  * {
  * "shift": "AFTERNOON_GRANDS",
  * "cover": 1
  * },
  * {
  * "shift": "CLOSE",
  * "cover": 1
  * }
  * ]
  * }
  */

case class Cover(shift: ShiftDefinition, cover: Int)

case class Day(override val id: String, shifts:List[Cover]) extends Identifiable[String](id)

case class Week(override val id: String, days:List[Day]) extends Identifiable[String](id)
