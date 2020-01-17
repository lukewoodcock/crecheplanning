package model2

case class SimpleModel(skills: String) {

}

case class Location(lat: Double, long: List[String])
case class Resident(name: String, age: Int, role: Option[String])
case class Place(name: String, location: Location, residents: Seq[Resident])

case class Model(skills: List[String], shifts: List[ShiftDefinition], contracts: List[Contract], families: List[Family]) {

}
