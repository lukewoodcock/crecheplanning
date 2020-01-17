package model2

import model.Identifiable

/**
  * {
  * "id": "OPEN",
  * "description": "Open crèche",
  * "startTime": "08:00:00",
  * "endTime": "09:30:00",
  * "skillsRequirements": [
  * "Organise"
  * ]
  * }
  */

case class ShiftDefinition(override val id: String, description: String, startTime: String, endTime: String, skillsRequirements:List[String]) extends Identifiable[String](id) {

}
