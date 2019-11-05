package utils

import java.text.SimpleDateFormat
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

}
