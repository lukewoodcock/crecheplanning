package utils

import java.util.Calendar

import model.ScheduledShift

object ShiftUtils {

  /***
    * Counts the number of scheduled shifts occurring on a given date
    * @param shifts list of ScheduledShift to count
    * @param date Calendar instance the check
    * @return the number (Int) of ScheduledShift on the given date
    */
  def numShiftsOnDay(shifts: List[ScheduledShift], date:Calendar):Int = shifts.filter(s => DateUtils.sameDay(s.date, date)).size

  /***
    * Checks a list of ScheduledShift to see if any occur on a given date
    * @param shifts
    * @param date
    * @return
    */
  def hasShiftOnDay(shifts: List[ScheduledShift], date:Calendar) = numShiftsOnDay(shifts, date) > 0

  /***
    * Filter a list of ScheduledShift by category and week of year
    * @param shifts
    * @param category
    * @param week
    * @return
    */
  def getShiftsByCategoryForWeek(shifts: List[ScheduledShift], category: String, week:Int):List[ScheduledShift] = {
    shifts.filter(s => s.date.get(Calendar.WEEK_OF_YEAR) == week)
        .filter(s => s.definition.category == category)//TODO use shift id with contract
//    mappedShifts.get(week) match {
//      case Some(r) => r.toList.filter(s => s.definition.category == shift.definition.category) //TODO use shift id with contract
//      case None => List[ScheduledShift]()
//    }
  }

  /***
    * Filter a list of ScheduledShift with a list of Calendar instances
    * @param dates
    * @param shifts
    * @return
    */
  def filterShiftsByDates(dates:List[Calendar], shifts:List[ScheduledShift]) = shifts
    .filter(s => {
      dates.filter(d => DateUtils.sameDay(s.date, d)).isEmpty
    })

  /***
    * Does what it says on the tin where week is week of year. TODO refactor to use Map
    * @param shifts
    * @return
    */
  def groupShiftsByWeek(shifts:Array[ScheduledShift]) = {
    val groups = shifts.groupBy(_.date.get(Calendar.WEEK_OF_YEAR))
    val out = groups.map{ case (key, value) => value.head.date.get(Calendar.YEAR).toString.concat("_").concat(key.toString) -> value.toList}
    out.toList.sortBy(_._1)
  }
}
