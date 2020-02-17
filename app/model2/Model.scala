package model2

import model.Identifiable
import model2.shifts.ShiftDefinition

case class Model(skills: List[String], shifts: List[ShiftDefinition], contracts: List[Contract], families: List[Family])

//Scheduling
case class Cover(shiftDefinitionId: String, cover: Int)
case class Day(override val id: Int, shifts:List[Cover]) extends Identifiable[Int](id)
case class WeekDefinition(override val id: String, days:List[Day]) extends Identifiable[String](id)
case class Absence(date: String, familyId: String, shiftId: Option[String])
case class CoverRequirements(year: Int, month: Int, weekDefinitions: List[WeekDefinition])
case class ScheduleRequirements(shifts: List[ShiftDefinition], coverRequirements: List[CoverRequirements], shiftAbsences: Option[List[Absence]], dateAbsences: Option[List[Absence]])
