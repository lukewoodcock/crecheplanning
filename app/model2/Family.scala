package model2

import model.Identifiable


/**
  * {
  * "id": 0,
  * "contractId": 0,
  * "name": "EMMA",
  * "skills": [
  * "Organise",
  * "Guard_Moyens",
  * "Gym_Moyens"
  * ]
  * }
  */

case class Family(override val id: String, contractId: Int, name: String, skills:List[String]) extends Identifiable[String](id)
