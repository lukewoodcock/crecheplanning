package model

import scala.collection.mutable.ListBuffer

object Family {

}

case class Family(override val id: String) extends Identifiable[String](id) {
  val shifts = ListBuffer[Shift]()
  val noCanDo = ListBuffer[Shift]()
}

//case class Shift(override val id: String, val shiftType: ShiftType, val family: String) extends Identifiable[String](id)
//
//case class ShiftType(val shiftType: String, val duration: Int)