package model

object Shift {

  object NAMES {
    val OPENING = "OPENING"
    val MORNING = "MORNING"
    val AFTERNOON = "AFTERNOON"
    val CLOSING = "CLOSING"
  }
  object TYPES {
    val GUARD = "GUARD"
    val ORGANISE = "ORGANISE"
  }
}
case class Shift(override val id: String, shiftType: ShiftType, date:Int, family: Option[Family]) extends Identifiable[String](id)

case class ShiftType(override val id: String, shiftType: String, duration: Double) extends Identifiable[String](id)
