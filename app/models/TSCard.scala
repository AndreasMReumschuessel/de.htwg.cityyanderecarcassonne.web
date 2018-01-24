package models

case class TSCard (
               position: String,
               name: String,
               orientation: Int,
               regions: Map[String, String]
             )