package model

object Shift {
  val OPENING = "OPENING"
  val MORNING = "MORNING"
  val AFTERNOON = "AFTERNOON"
  val CLOSING = "CLOSING"
}
case class Shift(override val id: String, val shiftType: ShiftType, val date:Int, val family: Option[Family]) extends Identifiable[String](id)

case class ShiftType(val shiftType: String, val duration: Double)
