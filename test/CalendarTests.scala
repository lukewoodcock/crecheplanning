import java.text.SimpleDateFormat

import org.joda.time.DateTime
import org.scalatest.FunSuite
import utils.DateUtils


class CalendarTests extends FunSuite {

  //TODO - add restrictions / preferences
  //TODO - prioritise families for week if previous week they don't meet weekly quota

  test("calendar date") {
    import java.util.Calendar
    // create a calendar// create a calendar

    val cal = Calendar.getInstance

    // print current time
    println("Current year is :" + cal.getTime)

    // set the year,month and day to something else
    cal.set(1995, 5, 25)

    // print the result
    println("Altered year is :" + cal.getTime)

    val date: DateTime = new DateTime(1995, 10, 10, 7, 15)
    cal.setTime(date.toDate)

    val anotherDate: DateTime = new DateTime(1995, 10, 10, 7, 35)
    cal.setTime(anotherDate.toDate)

    println("date is before anotherDate = " + (date.compareTo(anotherDate) < 0))


    // print the new time
    println("After setting Time:  " + cal.getTime)
    println("as string:  " + DateUtils.getDateAsString(cal.getTime))
  }

  test("hour string manipulation") {
    import java.util.Calendar
    val date = Calendar.getInstance().getTime()
    val dateFormat = new SimpleDateFormat("yyyy-mm-dd")
    val strDate = dateFormat.format(date)

    val finalDateStr = strDate.concat(" ").concat("08:00:00")
    val finalDate = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").parse(finalDateStr)
    println(finalDateStr)
    println(finalDate.toString)

    println(new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
      .parse(new SimpleDateFormat("yyyy-mm-dd")
        .format(date)
        .concat(" ")
        .concat("09:00:00")
      ))

    //TODO debug why there are random dates here

    println(date)
  }
}
