package model

import java.util.Calendar

object Shift {

  object NAMES {
    val OPENING = "OPENING"
    val MORNING_MOYENS = "MORNING_MOYENS"
    val MORNING_GRANDS = "MORNING_GRANDS"
    val AFTERNOON_MOYENS = "AFTERNOON_MOYENS"
    val AFTERNOON_GRANDS = "AFTERNOON_GRANDS"
    val CLOSING = "CLOSING"
  }

  object TYPES {
    val GUARD = "GUARD"
    val ORGANISE = "ORGANISE"
  }
}
case class Shift(override val id: String, shiftType: ShiftType, date:Calendar, family: Option[Family]) extends Identifiable[String](id)

case class ShiftType(override val id: String, shiftType: String, duration: Double) extends Identifiable[String](id)
