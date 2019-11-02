package controllers

import play.api.mvc._
import javax.inject._
import data.DataManager
import model.{Family, Shift, ShiftType}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  val dm = new DataManager()
  val shifts: List[Shift] = dm.mockWeek()
  val families: List[Family] = dm.mockFamilies()

  def doWork(shifts:List[Shift], families:List[Family]) : List[(Shift, Option[Family])] = {
    for(s <- shifts) yield {
      // remove unavailable families
      val contenders = families
//        .filter(f =>
//          f.noCanDo.find(nS => nS.id == s.id).nonEmpty
//        )


      //find first family with no shifts for the week
      val toto = contenders.filter(f => f.shifts.isEmpty)
      if(toto.nonEmpty) {
        toto.head.shifts += s
        (s, Some(toto.head))
      }
      else {
        // get SHIFT type
        // find first family with NO shifts of that type for the week && no shifts that day
        val tata = contenders
          // only take contenders with no shifts that day
          .filter(f =>
            !f.shifts.toList.exists(fs => fs.date == s.date)
          )
          // only take contenders with no shifts of that id
          .filter(f =>
            !f.shifts.toList.exists(fs => {
              fs.shiftType.id.equals(s.shiftType.id)
            })
          )
          // remove contenders that have already reached threshold for each shift type
          .filter(f =>
          s.shiftType match {
            case ShiftType(_, Shift.TYPES.ORGANISE, _) => !f.hasOrganise(1)
            case ShiftType(_, Shift.TYPES.GUARD, _) => !f.hasGuard(2)
          }
        )

        if(tata.nonEmpty) {
          tata.head.shifts += s
          (s, Some(tata.head))
        }
        else {
          s.shiftType match {
            // if SHIFT.TYPE is OPEN_CLOSE (threshold is 1 shift per week per child)
            // FLAG for REQUEST_EXTRA
            case ShiftType(_, Shift.TYPES.ORGANISE, _) =>
              (s, None)
            // find first family with only 1 GUARD
            case _ =>
              val titi = contenders
                .filter(f =>
                  f.shifts.count(fs =>
                    fs.shiftType match {
                      case ShiftType(_, Shift.TYPES.GUARD, _) => false
                      case _ => true
                    }) <= 1
              )
              if(titi.nonEmpty) {
                titi.head.shifts += s
                (s, Some(titi.head))
              }
              else (s, None)
          }
        }
      }

//      //find first family with no shifts for the week
//      contenders.filter(f =>
//        f.shifts.isEmpty
//      ) match {
//        case head :: tail => {
//          head.shifts += s
//          (s, Some(head))
//        }
//        case head :: Nil => {
//          head.shifts += s
//          (s, Some(head))
//        }
//        case Nil => {
//          // get SHIFT type
//          // find first family with NO shifts of that type for the week && no shifts that dayx
//          contenders.filter(f =>
//            f.shifts
//              .filter(fs => fs.date != s.date)
//              .filter(fs => fs.shiftType != s.shiftType)
//              .isEmpty
//          ) match {
//            case head :: tail => {
//              head.shifts += s
//              (s, Some(head))
//            }
//            case head :: Nil => {
//              head.shifts += s
//              (s, Some(head))
//            }
//            case Nil => {
//              s.shiftType match {
//                // if SHIFT.TYPE is OPEN_CLOSE (threshold is 1 shift per week per child)
//                // FLAG for REQUEST_EXTRA
//                case ShiftType(Shift.OPENING, _) | ShiftType(Shift.CLOSING, _) => (s, None)
//                // find first family with only 1 GUARD
//                case _ => {
//                  contenders.filter(f =>
//                    f.shifts.filter(fs =>
//                      fs.shiftType match {
//                        case ShiftType(Shift.OPENING, _) | ShiftType(Shift.CLOSING, _) => false
//                        case _ => true
//                      }
//                    )
//                    .size <= 1
//                  ) match {
//                    case head :: tail => {
//                      head.shifts += s
//                      (s, Some(head))
//                    }
//                    case head :: Nil => {
//                      head.shifts += s
//                      (s, Some(head))
//                    }
//                    case Nil => (s, None)
//                  }
//                }
//              }
//            }
//          }
//        }
//      }
    }
  }

//  for(s <- shifts) {
//    println(s)
//  }
//  println(shifts)

  val result: List[(Shift, Option[Family])] = doWork(shifts, families)
  for(s <- result) {
    println("Shift:" + s)
  }

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index(result.toString()))
  }

}
