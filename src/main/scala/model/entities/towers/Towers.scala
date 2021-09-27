package model.entities.towers

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, ShotAbility, SightAbility }
import model.entities.bullets.Bullets.{ BasicBullet, CannonBall, Dart, IceBall }
import model.entities.towers.Towers.{ BaseTower, BasicTower, CannonTower, IceTower }
import utils.Constants.Entities.Towers._
import utils.Constants.Entities.Bullets._
import utils.Constants.Entities.defaultPosition

import scala.language.{ implicitConversions, postfixOps }

object Towers {

  /**
   * A [[Tower]] is an [[Entity]] with the ability of see balloon object in its sight range
   */
  trait Tower extends Entity with SightAbility with ShotAbility {
    type Boundary = (Double, Double)

    override def rotateTo(dir: Vector2D): Tower

    override def withSightRangeOf(radius: Double): Tower

    override def withShotRatioOf(ratio: Double): Tower
  }

  /**
   * A [[BasicTower]] is an instance of a [[Tower]] with a defined:
   *   - position in the map
   *   - sight range to detect near balloon
   *   - shot ratio to shot bullets at a certain frequency
   */
  abstract class BasicTower(
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double,
      override val direction: Vector2D,
      override val boundary: (Double, Double) = towerDefaultBoundary)
      extends Tower {

    override def rotateTo(dir: Vector2D): Tower = instance(position, sightRange, shotRatio, dir)

    override def in(pos: Vector2D): Tower = instance(pos, sightRange, shotRatio, direction)

    override def withSightRangeOf(radius: Double): Tower =
      instance(position, radius, shotRatio, direction)

    override def withShotRatioOf(ratio: Double): Tower =
      instance(position, sightRange, ratio, direction)

    def instance(pos: Vector2D, range: Double, ratio: Double, dir: Vector2D): Tower
  }

  case class BaseTower(
      override val bullet: Dart,
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double,
      override val direction: Vector2D)
      extends BasicTower(position, sightRange, shotRatio, direction) {

    override def instance(pos: Vector2D, range: Double, ratio: Double, dir: Vector2D): BaseTower =
      BaseTower(bullet, pos, range, ratio, dir)
  }

  case class IceTower(
      override val bullet: IceBall,
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double,
      override val direction: Vector2D)
      extends BasicTower(position, sightRange, shotRatio, direction) {

    override def instance(pos: Vector2D, range: Double, ratio: Double, dir: Vector2D): IceTower =
      IceTower(bullet, pos, range, ratio, dir)
  }

  case class CannonTower(
      override val bullet: CannonBall,
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double,
      override val direction: Vector2D)
      extends BasicTower(position, sightRange, shotRatio, direction) {

    override def instance(pos: Vector2D, range: Double, ratio: Double, dir: Vector2D): CannonTower =
      CannonTower(bullet, pos, range, ratio, dir)
  }

}

object TowerTypes {

  sealed trait Ammo {
    def bullet: BasicBullet
  }

  sealed class TowerAmmo(override val bullet: BasicBullet) extends Ammo

  case object Base
      extends TowerAmmo(
        Dart(bulletDefaultDamage, defaultPosition, bulletDefaultSpeed, bulletDefaultBoundary)
      )

  case object Ice
      extends TowerAmmo(
        IceBall(
          bulletDefaultDamage,
          defaultPosition,
          bulletDefaultSpeed,
          bulletDefaultRadius,
          bulletFreezingTime,
          bulletDefaultBoundary
        )
      )

  case object Cannon
      extends TowerAmmo(
        CannonBall(
          bulletDefaultDamage,
          defaultPosition,
          bulletDefaultSpeed,
          bulletDefaultRadius,
          bulletDefaultBoundary
        )
      )

  object Ammo {
    // def apply(bullet: BasicBullet): TowerAmmo = new TowerAmmo(bullet)
    def unapply(ammo: TowerAmmo): Option[BasicBullet] = Some(ammo.bullet)
  }

  implicit class TowerType(ammo: TowerAmmo) {

    def tower: BasicTower = ammo match {
      case Ammo(b) if b.isInstanceOf[Dart] =>
        BaseTower(
          b.asInstanceOf[Dart],
          defaultPosition,
          towerDefaultSightRange,
          towerDefaultShotRatio,
          towerDefaultDirection
        )
      case Ammo(b) if b.isInstanceOf[IceBall] =>
        IceTower(
          b.asInstanceOf[IceBall],
          defaultPosition,
          towerDefaultSightRange,
          towerDefaultShotRatio,
          towerDefaultDirection
        )
      case Ammo(b) if b.isInstanceOf[CannonBall] =>
        CannonTower(
          b.asInstanceOf[CannonBall],
          defaultPosition,
          towerDefaultSightRange,
          towerDefaultShotRatio,
          towerDefaultDirection
        )
      case _ =>
        BaseTower(
          Dart(bulletDefaultDamage, defaultPosition, bulletDefaultSpeed, bulletDefaultBoundary),
          defaultPosition,
          towerDefaultSightRange,
          towerDefaultShotRatio,
          towerDefaultDirection
        )
    }
  }
}
