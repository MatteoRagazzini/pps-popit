package model.maps

import model.Positions.Vector2D
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Tracks.Directions.{ Direction, Down, Left, Right, Up }
import utils.Commons.Screen.cellSize

import scala.language.postfixOps
import scala.util.Random

object Grids {

  /**
   * Represents the logical structure of the game beneath the map.
   */
  trait Grid {

    /** Defines the number of cells on the x-axis */
    def width: Int

    /** Defines the number of cells on the y-axis */
    def height: Int

    /** Returns the sequence of all the cells in the grid. */
    def cells: Seq[Cell]
  }

  /**
   * Represents a normal grid formed of square cells.
   *
   * @param width,
   *   the number of cells on the x-axis
   * @param height,
   *   the number of cells on the y-axis
   */
  case class GridMap(override val width: Int, override val height: Int) extends Grid {

    override def cells: Seq[Cell] = for {
      x <- 0 until width
      y <- 0 until height
    } yield Cell(x, y)
  }

  object Grid {

    def apply(width: Int, height: Int): Grid =
      GridMap(width, height)

    /**
     * Represents the DSL of the grid.
     *
     * @param grid,
     *   the base grid on which it relies on
     */
    implicit class RichGrid(grid: Grid) {

      def border(direction: Direction): Seq[Cell] = direction match {
        case Left  => grid.cells.filter(_.x == 0)
        case Up    => grid.cells.filter(_.y == 0)
        case Right => grid.cells.filter(_.x == grid.width - 1)
        case Down  => grid.cells.filter(_.y == grid.height - 1)
        case _     => grid cells
      }

      def randomInBorder(direction: Direction): Cell =
        (Random shuffle grid.border(direction)) head

      def specificCell(position: Vector2D): Cell =
        GridCell(position.x / cellSize, position.y / cellSize)

      implicit private def toInt: Double => Int = _.toInt
    }
  }

}
