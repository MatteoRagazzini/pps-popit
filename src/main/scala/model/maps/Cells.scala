package model.maps

import model.Positions._
import model.Positions.Vector2D
import model.maps.Tracks.Directions.{ Direction, Down, Left, None, Right, Up }
import model.maps.Tracks.Track
import utils.Commons.Screen.cellSize

import scala.language.{ implicitConversions, postfixOps }

object Cells {

  /**
   * Represents a cell with two integer coordinates.
   */
  trait Cell {

    /** The x-coordinate of the cell */
    def x: Int

    /** The y-coordinate of the cell */
    def y: Int

    /** The direction of the cell */
    def direction: Direction

    /** Changes the direction of the cell */
    def direct(dir: Direction): Cell
  }

  object Cell {

    def apply(x: Int, y: Int): Cell =
      GridCell(x, y)
  }

  /**
   * Represents a [[Cell]] in a grid.
   *
   * @param x,
   *   the x-coordinate of the cell
   * @param y,
   *   the y-coordinate of the cell
   */
  case class GridCell(
      override val x: Int,
      override val y: Int,
      override val direction: Direction = None)
      extends Cell {
    override def direct(dir: Direction): Cell = GridCell(x, y, dir)
  }

  implicit def toPosition(cell: Cell): Vector2D = cell.centralPosition

  /**
   * Enriched cell, with pimped operators.
   *
   * @param cell,
   *   the implicitly enriched cell.
   */
  implicit class RichCell(cell: Cell) {

    def turnRight(): Cell = cell.direct(cell.direction.turnRight)

    def turnLeft(): Cell = cell.direct(cell.direction.turnLeft)

    def nextOnTrack: Cell = cell match {
      case GridCell(x, y, dir) =>
        dir match {
          case Up    => GridCell(x, y - 1)
          case Down  => GridCell(x, y + 1)
          case Left  => GridCell(x - 1, y)
          case Right => GridCell(x + 1, y)
          case _     => cell
        }
    }

    def directTowards(other: Cell): Cell = other match {
      case GridCell(x, _, _) if x == cell.x + 1 => cell direct Right
      case GridCell(x, _, _) if x == cell.x - 1 => cell direct Left
      case GridCell(_, y, _) if y == cell.y + 1 => cell direct Down
      case GridCell(_, y, _) if y == cell.y - 1 => cell direct Up
      case _                                    => cell
    }

    def contains(position: Vector2D): Boolean =
      (cell.x + 1) * cellSize >= position.x &&
        cell.x * cellSize < position.x &&
        (cell.y + 1) * cellSize >= position.y &&
        cell.y * cellSize < position.y

    def in(track: Track): Boolean = track.cells.map(c => (c.x, c.y)).contains((cell.x, cell.y))

    def topLeftPosition: Vector2D = (cell.x * cellSize, cell.y * cellSize)

    def centralPosition: Vector2D = this.topLeftPosition + (cellSize / 2, cellSize / 2)

    def vectorialPosition(previous: Direction)(percentage: Double): Vector2D = percentage match {
      case x if x < 0.5 => cellOnTrackPosition(previous)(percentage)
      case _            => cellOnTrackPosition(cell direction)(percentage)
    }

    def cellOnTrackPosition(direction: Direction)(percentage: Double): Vector2D = direction match {
      case Up    => cell.topLeftPosition + (cellSize / 2, cellSize * (1 - percentage))
      case Down  => cell.topLeftPosition + (cellSize / 2, cellSize * percentage)
      case Left  => cell.topLeftPosition + (cellSize * (1 - percentage), cellSize / 2)
      case Right => cell.topLeftPosition + (cellSize * percentage, cellSize / 2)
      case _     => cell.topLeftPosition + (cellSize / 2, cellSize / 2)
    }

  }
}
