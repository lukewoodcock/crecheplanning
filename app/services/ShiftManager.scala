package services

import model.{Family, Shift, ShiftType}

object ShiftManager {

  def removeByDate(families:List[Family], date:Int): List[Family] = families.filter(f =>
    !f.shifts.toList.exists(fs => fs.date == date)
  )

  def removeByShiftId(families:List[Family], id:String): List[Family] = families.filter(f =>
    !f.shifts.toList.exists(fs => fs.shiftType.id.equals(id))
  )

  def removeByShiftCategory(families:List[Family], shiftType:ShiftType, limit:Int): List[Family] = families.filter(f =>
    f.shifts.count(s => s.shiftType match {
      case ShiftType(_, shiftType, _) => true
      case _ => false
    }) < limit
  )

  def autoFill(shifts:List[Shift], families:List[Family]) : List[(Shift, Option[Family])] = {
    for(s <- shifts) yield {
      // remove unavailable families
      val contenders = families.toList
      //        .filter(f =>
      //          f.noCanDo.find(nS => nS.id == s.id).nonEmpty
      //        )


      //find first family with no shifts for the week
      val toto = contenders.filter(f => f.shifts.isEmpty)
      if(toto.nonEmpty) {
        toto.head.addShift(s)
        (s, Some(toto.head))
      }
      else {
        // get SHIFT type
        // find first family with NO shifts of that type for the week && no shifts that day
//        val tata = contenders
          // only take contenders with no shifts that day
//          .filter(f =>
//            !f.shifts.toList.exists(fs => fs.date == s.date)
//          )

//        val tata = removeByShiftCategory(
//          removeByShiftId(
//            removeByDate(contenders, s.date)
//            , s.shiftType.id)
//          , s.shiftType
//          , s.shiftType match {
//          case ShiftType(_, Shift.TYPES.ORGANISE, _) => 1
//          case ShiftType(_, Shift.TYPES.GUARD, _) => 2
//        })

//        val date = removeByDate(contenders, s.date)
//        if(date.isEmpty) {}


        val tata2 = removeByShiftId(
          removeByDate(contenders, s.date)
          , s.shiftType.id)
        val tata = s.shiftType match {
          case ShiftType(_, Shift.TYPES.ORGANISE, _) => tata2.filter(f => f.shifts.toList.count(sh => sh.shiftType.shiftType == s.shiftType.shiftType) < 1)
          case ShiftType(_, Shift.TYPES.GUARD, _) => tata2.filter(f => f.shifts.toList.count(sh => sh.shiftType.shiftType == s.shiftType.shiftType) < 2)
        }
          // only take contenders with no shifts of that id
//          .filter(f =>
//            !f.shifts.toList.exists(fs => {
//              fs.shiftType.id.equals(s.shiftType.id)
//            })
//          )
          // remove contenders that have already reached threshold for each shift type
//          .filter(f =>
//            s.shiftType match {
//              case ShiftType(_, Shift.TYPES.ORGANISE, _) => !f.hasOrganise(1)
//              case ShiftType(_, Shift.TYPES.GUARD, _) => !f.hasGuard(2)
//            }
//          )
//          .filter(f =>
//            f.shifts.size < 3
//          )

        if(tata.nonEmpty) {
          tata.head.addShift(s)
          (s, Some(tata.head))
        }
        else {
          s.shiftType match {
            // if SHIFT.TYPE is OPEN_CLOSE (threshold is 1 shift per week per child)
            // FLAG for REQUEST_EXTRA
            case ShiftType(_, Shift.TYPES.ORGANISE, _) =>
              (s, None)
            // find first family with only 1 GUARD
            case ShiftType(_, Shift.TYPES.GUARD, _) =>

//              val titi = removeByShiftCategory(families, s.shiftType, 2)
//              val titi = contenders.filter(f => f.shifts.toList.count(sh => sh.shiftType.shiftType == s.shiftType.shiftType) < 2)
              val titi = removeByDate(contenders, s.date).filter(f => f.shifts.toList.count(sh => sh.shiftType.shiftType == s.shiftType.shiftType) < 2)

//              val titi = contenders
//                .filter(f =>
//                  f.shifts.count(fs =>
//                    fs.shiftType match {
//                      case ShiftType(_, Shift.TYPES.GUARD, _) => false
//                      case _ => true
//                    }) <= 1
//                )
              if(titi.nonEmpty) {
                titi.head.addShift(s)
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
}
