package utils

import java.text.SimpleDateFormat
import java.time._
import java.util.Calendar
import java.util.Date

object DateUtils {

  val DATE_FORMAT = "EEE, MMM dd, yyyy"

  def getDateAsString(d: Date): String = {
    val dateFormat = new SimpleDateFormat(DATE_FORMAT)
    dateFormat.format(d)
  }

  def convertStringToDate(s: String): Date = {
    val dateFormat = new SimpleDateFormat(DATE_FORMAT)
    dateFormat.parse(s)
  }

  def convertDateStringToLong(dateAsString: String): Long = {
    convertStringToDate(dateAsString).getTime
  }

  def convertLongToDate(l: Long): Date = new Date(l)

  def now(): Date = Calendar.getInstance.getTime

  def sameDay(cal1:Calendar, cal2:Calendar):Boolean = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

  def addWeeks(cal:Calendar, amount:Int) = {
    cal.add(Calendar.DATE, 7 * amount)
    cal
  }

  def isWeekend(day: LocalDate) =
    day.getDayOfWeek == DayOfWeek.SATURDAY ||
      day.getDayOfWeek == DayOfWeek.SUNDAY

  def datesInYear(year: Year) = (1 to year.length()).map(year.atDay)

  def getMonth(month:Month) = datesInYear(Year.now())
    .filter(d => d.getMonth == month)
    .filter(d => !isWeekend(d))
    .map(d => getCalendarDay(d))

  def getCalendarDay(localDate:LocalDate) = {
    val out = Calendar.getInstance()
    out.setTime(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
    out
  }
}
