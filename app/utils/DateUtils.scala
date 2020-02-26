package utils

import java.text.SimpleDateFormat
import java.time._
import java.util.Calendar
import java.util.Date

object DateUtils {

  private val DATE_FORMAT = "EEE, MMM dd, yyyy h:mm a"

  /***
    * Return astring representation of Date using "EEE, MMM dd, yyyy h:mm a" date format
    * @param d
    * @return
    */
  def getDateAsString(d: Date): String = {
    val dateFormat = new SimpleDateFormat(DATE_FORMAT)
    dateFormat.format(d)
  }

  /***
    * Check if to Calendar instances are on the same day
    * @param cal1
    * @param cal2
    * @return true if to Calendar instances are on the same day
    */
  def sameDay(cal1:Calendar, cal2:Calendar):Boolean = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

  /***
    * Create a collection of LocalDate for an entire year
    * @param year
    * @return
    */
  def datesInYear(year: Year) = (1 to year.length()).map(year.atDay)

  /**
    * Get a Calendar to a LocalDate
    * @param localDate
    * @return
    */
  def getCalendarDay(localDate:LocalDate) = {
    val out = Calendar.getInstance()
    out.setTime(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
    out
  }
}
