package controllers

import collection.JavaConverters._
import javax.inject.{Inject, Singleton}

import de.htwg.cityyanderecarcassonne.Carcassonne
import de.htwg.cityyanderecarcassonne.controller.ICarcassonneController
import de.htwg.cityyanderecarcassonne.model.ICard
import de.htwg.cityyanderecarcassonne.model.IPlayer
import de.htwg.cityyanderecarcassonne.model.IPosition
import de.htwg.cityyanderecarcassonne.model.IRegion
import de.htwg.cityyanderecarcassonne.view.tui.TextUI
import models._
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.libs.json._
import play.api.libs.functional.syntax._


import scala.collection.mutable.ListBuffer

@Singleton
class CarcassonneWebController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  private val carcassonne: Carcassonne = Carcassonne.getInstance(15, 15, false, true)
  private val controller: ICarcassonneController = carcassonne.getController

  // Json Converters
  implicit val jsonCurrentCardWrites = Json.writes[CurrentCard]
  implicit val jsonPlayerWrites = Json.writes[Player]
  implicit val jsonPossCardPosWrites = Json.writes[PossCardPos]
  implicit val jsonTSCardWrites = Json.writes[TSCard]
  implicit val jsonTownsquareWrites = Json.writes[Townsquare]

  private var playerIdMap: Map[IPlayer, String] = Map()
  private var lastCardPosition: String = _

  private def execCmd(cmd: String): Unit = {
    val tui: TextUI = carcassonne.getTui
    tui.processInput(cmd)
  }

  def cycarcassonne(cmd: String) = Action {
    execCmd(cmd)
    Ok(views.html.cyc.render(controller))
  }

  def getGameStatus = Action {
    okGetGameStatus
  }

  private def okGetGameStatus = Ok(controller.getStatus.toString)

  def addPlayer(name: String) = Action {
    /*implicit val jsonPlayerWrites: Writes[Player] = (
      (JsPath \ "pid").write[String] and
        (JsPath \ "name").write[String] and
        (JsPath \ "meeple").write[String] and
        (JsPath \ "score").write[String]
      )(unlift(Player.unapply))*/
    controller.addPlayer(name)
    val controllerPlayer: IPlayer =
      controller.getPlayers.get(controller.getPlayers.size - 1)
    val pid: String = "pid_" + playerIdMap.size
    playerIdMap += (controllerPlayer -> pid)
    Ok(Json.toJson(convertPlayer(controllerPlayer)))
  }

  def getAllPlayers = Action {
    val players: scala.collection.mutable.Buffer[IPlayer] = controller.getPlayers.asScala
    val jsonPlayers: ListBuffer[Player] = new ListBuffer[Player]
    for (player <- players) {
      jsonPlayers += convertPlayer(player)
    }
    Ok(Json.toJson(jsonPlayers.toList))
  }

  private def convertPlayer(player: IPlayer): Player = {
    val jsonPlayer: Player = Player(
      playerIdMap(player),
      player.getName,
      player.getSumMeeples,
      player.getScore
    )
    jsonPlayer
  }

  def createGame = Action {
    controller.create()
    controller.startRound()
    Ok("ok")
  }

  def getCurrentPlayerId = Action {
    Ok(playerIdMap(controller.getCurrentPlayer))
  }

  def getCurrentCard = Action {
    okGetCurrentCard
  }

  def okGetCurrentCard = Ok(Json.toJson(cardConvert(controller.cardOnHand())))

  def rotateCard(direction: String) = Action {
    if ("left" == direction) {
      controller.rotateCardLeft()
    } else if ("right" == direction) {
      controller.rotateCardRight()
    } else {
      BadRequest("Unknown direction. Only \"left\" and \"right\" possible.")
    }
    println(controller.cardOnHand().getLeftMiddle.getID)
    okGetCurrentCard
  }

  private def cardConvert(icard: ICard): CurrentCard = {
    val currCard: CurrentCard = new CurrentCard(
      cardConvertName(icard),
      icard.getOrientation
    )
    currCard
  }

  private def cardConvertName(icard: ICard): String =
    icard.toString.substring(icard.toString.lastIndexOf(" ") + 1)

  def getRemainingCards() = Action {
    Ok(String.valueOf(controller.getRemainingCards))
  }

  def getCardPossibilities = Action {
    val possMap: scala.collection.mutable.Map[IPosition, String] =
      controller.getCardPossibilitiesMap(controller.cardOnHand()).asScala
    val possList: ListBuffer[PossCardPos] = new ListBuffer()
    for ((key, value) <- possMap) {
      val cardPos: PossCardPos = new PossCardPos(
        value,
        key.getY + "_" + key.getX
      )
      possList += cardPos
    }
    Ok(Json.toJson(possList.toList))
  }

  def placeCard(selection: String, position: String) = Action {
    lastCardPosition = position
    controller.placeCard(controller.cardOnHand(), selection)
    okGetGameStatus
  }

  def getMeeplePossibilities = Action {
    val card: ICard = controller.cardOnHand()

    val possMap: scala.collection.mutable.Map[IRegion, String] = controller.getRegionPossibilitiesMap(card).asScala

    var tmpTsCardMap: Map[String, String] = Map()
    for ((key, value) <- possMap) {
      if (key == card.getLeftTop) tmpTsCardMap += ("LT" -> value)
      if (key == card.getLeftMiddle) tmpTsCardMap += ("LM" -> value)
      if (key == card.getLeftBelow) tmpTsCardMap += ("LB" -> value)

      if (key == card.getBelowLeft) tmpTsCardMap += ("BL" -> value)
      if (key == card.getBelowMiddle) tmpTsCardMap += ("BM" -> value)
      if (key == card.getBelowRight) tmpTsCardMap += ("BR" -> value)

      if (key == card.getCenterMiddle) tmpTsCardMap += ("C" -> value)

      if (key == card.getTopLeft) tmpTsCardMap += ("TL" -> value)
      if (key == card.getTopMiddle) tmpTsCardMap += ("TM" -> value)
      if (key == card.getTopRight) tmpTsCardMap += ("TR" -> value)

      if (key == card.getRightTop) tmpTsCardMap += ("RT" -> value)
      if (key == card.getRightMiddle) tmpTsCardMap += ("RM" -> value)
      if (key == card.getRightBelow) tmpTsCardMap += ("RB" ->  value)
    }

    val tscard: TSCard = new TSCard(
      lastCardPosition,
      cardConvertName(card),
      card.getOrientation,
      tmpTsCardMap
    )

    Ok(Json.toJson(tscard))
  }

  def placeMeeple(selection: String) = Action {
    controller.placeMeeple(controller.getCurrentPlayer, controller.cardOnHand(), selection)
    okGetGameStatus
  }

  def getTownsquare = Action {
    val cts: de.htwg.cityyanderecarcassonne.model.townsquare.Townsquare = controller.getTownsquare

    val tsCardsTmpList: ListBuffer[TSCard] = ListBuffer()
    for (x <- 0 until cts.getDimX; y <- 0 until cts.getDimY) {
      val ctsCard: ICard = cts.getCard(x, y)
      if (ctsCard != null) {
        var tmpTsCardMap: Map[String, String] = Map()
        if (ctsCard.getLeftTop.getPlayer != null)
          tmpTsCardMap += ("LT" -> playerIdMap(ctsCard.getLeftTop.getPlayer))
        if (ctsCard.getLeftMiddle.getPlayer != null)
          tmpTsCardMap += ("LM" -> playerIdMap(ctsCard.getLeftMiddle.getPlayer))
        if (ctsCard.getLeftBelow.getPlayer != null)
          tmpTsCardMap += ("LB" -> playerIdMap(ctsCard.getLeftBelow.getPlayer))

        if (ctsCard.getTopLeft.getPlayer != null)
          tmpTsCardMap += ("TL" -> playerIdMap(ctsCard.getTopLeft.getPlayer))
        if (ctsCard.getTopMiddle.getPlayer != null)
          tmpTsCardMap += ("TM" -> playerIdMap(ctsCard.getTopMiddle.getPlayer))
        if (ctsCard.getTopRight.getPlayer != null)
          tmpTsCardMap += ("TR" -> playerIdMap(ctsCard.getTopRight.getPlayer))

        if (ctsCard.getCenterMiddle.getPlayer != null)
          tmpTsCardMap += ("C" -> playerIdMap(ctsCard.getCenterMiddle.getPlayer))

        if (ctsCard.getBelowLeft.getPlayer != null)
          tmpTsCardMap += ("BL" -> playerIdMap(ctsCard.getBelowLeft.getPlayer))
        if (ctsCard.getBelowMiddle.getPlayer != null)
          tmpTsCardMap += ("BM" ->playerIdMap(ctsCard.getBelowMiddle.getPlayer))
        if (ctsCard.getBelowRight.getPlayer != null)
          tmpTsCardMap += ("BR" -> playerIdMap(ctsCard.getBelowRight.getPlayer))

        if (ctsCard.getRightTop.getPlayer != null)
          tmpTsCardMap += ("RT" -> playerIdMap(ctsCard.getRightTop.getPlayer))
        if (ctsCard.getRightMiddle.getPlayer != null)
          tmpTsCardMap += ("RM" -> playerIdMap(ctsCard.getRightMiddle.getPlayer))
        if (ctsCard.getRightBelow.getPlayer != null)
          tmpTsCardMap += ("RB" -> playerIdMap(ctsCard.getRightBelow.getPlayer))

        val card: TSCard = new TSCard(
          y + "_" + x,
          cardConvertName(ctsCard),
          ctsCard.getOrientation,
          tmpTsCardMap
        )

        tsCardsTmpList += card
      }
    }
    val ts: Townsquare = new Townsquare(tsCardsTmpList.toList)
    Ok(Json.toJson(ts))
  }

  def finishRound()= Action {
    controller.finishRound()
    Ok("ok")
  }
}
