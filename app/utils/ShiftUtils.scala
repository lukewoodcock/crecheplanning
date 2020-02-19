package utils

import java.util.Calendar

import model2.shifts.ScheduledShift

object ShiftUtils {

  def numShiftsOnDay(shifts: List[ScheduledShift], date:Calendar):Int = shifts.filter(s => DateUtils.sameDay(s.date, date)).size

  def hasShiftOnDay(shifts: List[ScheduledShift], date:Calendar) = numShiftsOnDay(shifts, date) > 0

  def getShiftsByCategoryForWeek(shifts: List[ScheduledShift], category: String, week:Int):List[ScheduledShift] = {
    shifts.filter(s => s.date.get(Calendar.WEEK_OF_YEAR) == week)
        .filter(s => s.definition.category == category)//TODO use shift id with contract
//    mappedShifts.get(week) match {
//      case Some(r) => r.toList.filter(s => s.definition.category == shift.definition.category) //TODO use shift id with contract
//      case None => List[ScheduledShift]()
//    }
  }

  def filterShiftsByDates(dates:List[Calendar], shifts:List[ScheduledShift]) = shifts
    .filter(s => {
      dates.filter(d => DateUtils.sameDay(s.date, d)).isEmpty
    })

  def groupShiftsByWeek(shifts:Array[ScheduledShift]) = {
    val groups = shifts.groupBy(_.date.get(Calendar.WEEK_OF_YEAR))
    val out = groups.map{ case (key, value) => value.head.date.get(Calendar.YEAR).toString.concat("_").concat(key.toString) -> value.toList}
    out.toList.sortBy(_._1)
  }
}
