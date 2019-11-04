package model

import scala.collection.mutable.ListBuffer

object Family {

}

case class Family(override val id: String) extends Identifiable[String](id) {
  val shifts: ListBuffer[Shift] = ListBuffer[Shift]()
  val noCanDo: ListBuffer[Shift] = ListBuffer[Shift]()


  def hasOrganise(n:Int):Boolean = shifts.filter(s => s.shiftType.shiftType.equals(Shift.TYPES.ORGANISE)).toList.size >= n
  def hasGuard(n:Int):Boolean = shifts.filter(s => s.shiftType.shiftType.equals(Shift.TYPES.GUARD)).toList.size >= n

  def addShift(shift:Shift):Family = {
    shifts += shift
    this
  }
}