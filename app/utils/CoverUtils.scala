package utils

import java.time.Year
import java.time.temporal.WeekFields
import java.util.Locale

import model.CoverRequirements
import model.{ScheduledShift, ShiftDefinition}
import utils.DateUtils.datesInYear

object CoverUtils {

  /***
    * Creates a collection of ScheduledShift
    * @param cover model definition
    * @param shiftDefinitions list of ShiftDefinition used by ScheduledShift
    * @return
    */
  def getMonth(cover: CoverRequirements, shiftDefinitions: List[ShiftDefinition]):Array[ScheduledShift] = {

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
                        .map(i => {
                          val ss = ScheduledShift(value.id.concat(": ").concat(DateUtils.getDateAsString(DateUtils.getCalendarDay(d).getTime)), value, DateUtils.getCalendarDay(d), None)
                          val start = ss.start
                          ss
                        })
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
}
