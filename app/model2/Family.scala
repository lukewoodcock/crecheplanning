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

case class Family(override val id: Int, contractId: Int, name: String, skills:List[String]) extends Identifiable[Int](id)
