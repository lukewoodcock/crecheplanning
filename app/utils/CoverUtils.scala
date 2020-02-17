package utils

import java.time.Year
import java.time.temporal.WeekFields
import java.util.{Calendar, Locale}

import model2.shifts.{ScheduledShift, ShiftDefinition}
import model2.{CoverRequirements, ScheduleRequirements}
import utils.DateUtils.datesInYear

object CoverUtils {

  def getMonth(cover: CoverRequirements, shiftDefinitions: List[ShiftDefinition]) = {

    val daysInMonth = datesInYear(Year.of(cover.year))
      .filter(d => d.getMonth.getValue == cover.month)

    val weekOfMonthField = WeekFields.of(Locale.getDefault).weekOfMonth()
    val dayOfWeek = WeekFields.of(Locale.getDefault).dayOfWeek()

    var weekDefMod = 0
    daysInMonth
      .toArray.flatMap(d => {
      val wom = d.get(weekOfMonthField) - 1
      val index = wom - (weekDefMod * wom)
      if (index == cover.weekDefinitions.size) {
        weekDefMod += 1
      }
      cover.weekDefinitions.lift(index) match {
        case Some(weekDefinition) => {
          weekDefinition.days.find(day => day.id == d.get(dayOfWeek)) match {
            case Some(day) => {
//              println("Found a day")

              //day to scheduled shifts
              val shifts = day.shifts.map(c => {
                shiftDefinitions.find(definition => definition.id == c.shiftDefinitionId) match {
                  case Some(value) => {
                    val out = new Array[ScheduledShift](c.cover)
                        .map(i => ScheduledShift(value.id.concat(": ").concat(DateUtils.getDateAsString(DateUtils.getCalendarDay(d).getTime)), value, DateUtils.getCalendarDay(d), None))
                    out

                  }
                }
              })
              shifts.flatten
            }
            case None => {
//              println("Non day founds")
              None
            }
          }
        }
        case None => {
          None
        }
      }
    })
  }

  def generateCalendar(model: ScheduleRequirements) = {
    model.coverRequirements.map(cover => {
      // get first day of month
      val date = Calendar.getInstance()
      date.set(Calendar.YEAR, cover.year)
      date.set(Calendar.MONTH, cover.month)
      val year = cover.year
      val month = date.get(Calendar.MONTH)
      //
    })
  }

  def getShiftsForDay(date: Calendar, model: ScheduleRequirements) = {
    val year = date.get(Calendar.YEAR)
    val month = date.get(Calendar.MONTH)
    val week = date.get(Calendar.WEEK_OF_YEAR)
    val day = date.get(Calendar.DAY_OF_YEAR)
    val result = model.coverRequirements.filter(cover => cover.year == year)
      .filter(cover => cover.month == month)
    //      .filter(cover => cover.)
  }

  def hasShiftOnDay(shifts: List[ScheduledShift], date: Calendar) = ShiftUtils.numShiftsOnDay(shifts, date) > 0

  def getShiftsByCategoryForWeek(shifts: List[ScheduledShift], category: String, week: Int): List[ScheduledShift] = {
    shifts.filter(s => s.date.getWeekYear == week)
      .filter(s => s.definition.category == category) //TODO use shift id with contract
    //    mappedShifts.get(week) match {
    //      case Some(r) => r.toList.filter(s => s.definition.category == shift.definition.category) //TODO use shift id with contract
    //      case None => List[ScheduledShift]()
    //    }
  }

  def filterShiftsByDates(dates: List[Calendar], shifts: List[ScheduledShift]) = shifts
    .filter(s => {
      dates.filter(d => DateUtils.sameDay(s.date, d)).isEmpty
    })
}
