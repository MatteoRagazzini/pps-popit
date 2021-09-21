package view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{ Render, RenderEntities, RenderMap }
import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Tracks.Directions.RIGHT
import scalafx.application.Platform
import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.Pane

import java.io.File

object View {

  object ViewActor {
    val canvas: Canvas = new Canvas(1200, 600)
    val board: Board = Board(Seq(canvas))

    def apply(): Behavior[Render] = Behaviors.setup { _ =>
      Behaviors.receiveMessage {
        case RenderEntities(entities: List[Entity]) =>
          Platform.runLater {
            entities foreach {
              case balloon: Balloon =>
                val img: File = new File("src/main/resources/images/balloons/RED.png")
                canvas.graphicsContext2D.setFill(new ImagePattern(new Image(img.toURI.toString)))
                canvas.graphicsContext2D.fillRect(
                  balloon.position.x,
                  balloon.position.y,
                  balloon.boundary * 0.75,
                  balloon.boundary
                )
              case _ => println("vaffa")
            }
          }
          Behaviors.same

        case RenderMap(grid, track) =>
          val cellSize: Int = 75
          Platform.runLater {
            val img: File = new File("src/main/resources/images/backgrounds/GRASS.png")
            grid.cells foreach { cell =>
              canvas.graphicsContext2D.setFill(new ImagePattern(new Image(img.toURI.toString)))
              canvas.graphicsContext2D.fillRect(
                cell.x * cellSize,
                cell.y * cellSize,
                cellSize,
                cellSize
              )
            }
            track.cells.prepended(GridCell(-1, 0, RIGHT)).sliding(2).foreach { couple =>
              val name: String =
                couple.head.direction.toString + "-" + couple.last.direction.toString + ".png"
              val img: File = new File("src/main/resources/images/roads/" + name)
              canvas.graphicsContext2D.setFill(new ImagePattern(new Image(img.toURI.toString)))
              val cell: Cell = couple.last
              canvas.graphicsContext2D
                .fillRect(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)

            }
          }
          Behaviors.same
        case _ => Behaviors.same
      }
    }
  }

  case class Board(var elements: Seq[Node]) extends Pane {
    children = elements

    def draw(entities: List[Any]): Unit =
      Platform.runLater {
        //entities foreach ...
      }
  }

}
