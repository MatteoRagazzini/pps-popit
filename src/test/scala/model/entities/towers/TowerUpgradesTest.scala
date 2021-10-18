package model.entities.towers

import model.entities.balloons.Balloons.{ Balloon, Simple }
import model.entities.balloons.balloontypes.CamoBalloons.CamoBalloon
import model.entities.bullets.Bullets.Dart
import model.entities.towers.PowerUps.{ BoostedTower, Camo, Damage, Ratio, Sight }
import model.entities.towers.TowerTypes.Arrow
import model.entities.towers.Towers.Tower
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.Constants.Entities.Bullets.bulletDefaultDamage

import scala.language.postfixOps

class TowerUpgradesTest extends AnyWordSpec with Matchers {

  val boostedSight: Double = 2.0
  val boostedRatio: Double = 1.0
  val sightRange: Double = 1.0
  val shotRatio: Double = 2.0
  val defaultLevel: Int = 1

  "According to the expected system behavior" when {
    "the tower ratio is boosted" should {
      "change the shooting frequency" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio
        (arrowTower boost Ratio).shotRatio shouldBe boostedRatio
        (arrowTower sightRange) shouldBe sightRange
      }
    }
    "the tower sight is boosted" should {
      "change the sight range" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio
        (arrowTower boost Sight).sightRange shouldBe boostedSight
        (arrowTower shotRatio) shouldBe shotRatio
      }
    }
    "the tower is completely boosted" should {
      "have the boosted values of sight and ratio" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio
        val boostedTower: Tower[Dart] = arrowTower boost Sight boost Ratio
        (boostedTower sightRange) shouldBe boostedSight
        (boostedTower shotRatio) shouldBe boostedRatio
      }
    }
    "the tower get the ratio boost" should {
      "detect a balloon that can't see before" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio

        val balloon: Balloon = Simple(position = (1.5, 1.5), boundary = (1.0, 1.0))
        (arrowTower canSee balloon) shouldBe false
        val boostedTower: Tower[Dart] = arrowTower boost Sight
        (boostedTower canSee balloon) shouldBe true
      }
    }
    "the tower get the damage boost" should {
      "increment the bullet damage value" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio

        arrowTower.bullet.damage shouldBe bulletDefaultDamage
        val boostedTower: Tower[Dart] = arrowTower boost Damage
        boostedTower.bullet.isInstanceOf[Dart] shouldBe true
        boostedTower.bullet.damage shouldBe (bulletDefaultDamage * Damage.factor)
      }
    }
    "a normal tower see a camo balloon" should {
      "not detect it, unless if is boosted" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio

        val camo: Balloon = CamoBalloon(Simple(position = (1.0, 1.0), boundary = (1.0, 1.0)))
        (arrowTower canSee camo) shouldBe false
        val boostedTower: Tower[Dart] = arrowTower boost Camo
        (boostedTower canSee camo) shouldBe true
        boostedTower.bullet.isInstanceOf[Dart] shouldBe true
      }
    }
    /*"the tower get the ratio power up" should {
      "not be boosted no more after 3 seconds" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) withSightRangeOf sightRange withShotRatioOf shotRatio

        val boostedTower: Tower[Dart] = arrowTower boost Ratio

        (boostedTower shotRatio) shouldBe boostedRatio
        (boostedTower sightRange) shouldBe sightRange

        (boostedTower isBoosted Ratio.time) shouldBe true
        waitSomeTime()
        (boostedTower isBoosted Ratio.time) shouldBe true
        waitSomeTime()
        (boostedTower isBoosted Ratio.time) shouldBe true
        waitSomeTime()
        (boostedTower isBoosted Ratio.time) shouldBe false
      }
      "return to the previous state after 3 seconds" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) withSightRangeOf sightRange withShotRatioOf shotRatio

        val boostedTower: Tower[Dart] = arrowTower boost Ratio

        (boostedTower shotRatio) shouldBe boostedRatio
        (boostedTower sightRange) shouldBe sightRange

        (boostedTower isBoosted Ratio.time) shouldBe true

        waitSomeTime()
        waitSomeTime()
        waitSomeTime()

        (boostedTower isBoosted Ratio.time) shouldBe false
        val previousTower: Tower[Dart] = arrowTower.reset()

        (previousTower sightRange) shouldBe sightRange
        (previousTower shotRatio) shouldBe shotRatio
      }
    }*/
  }

  "Implementing levels for powerups" when {
    "a tower is boosted, it" should {
      "increment its stats levels" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio

        arrowTower level "ratio" shouldBe defaultLevel
        arrowTower level "sight" shouldBe defaultLevel
        arrowTower level "damage" shouldBe defaultLevel

        var boostedTower: Tower[Dart] = arrowTower boost Ratio
        (boostedTower shotRatio) shouldBe boostedRatio
        boostedTower level "ratio" shouldBe defaultLevel + 1
        boostedTower level "sight" shouldBe defaultLevel
        boostedTower level "damage" shouldBe defaultLevel

        boostedTower = boostedTower boost Sight
        (boostedTower sightRange) shouldBe boostedSight

        boostedTower level "ratio" shouldBe defaultLevel + 1
        boostedTower level "sight" shouldBe defaultLevel + 1
        boostedTower level "damage" shouldBe defaultLevel

        boostedTower = boostedTower boost Damage
        boostedTower level "ratio" shouldBe defaultLevel + 1
        boostedTower level "sight" shouldBe defaultLevel + 1
        boostedTower level "damage" shouldBe defaultLevel + 1

        boostedTower = boostedTower boost Camo
        boostedTower level "ratio" shouldBe defaultLevel + 1
        boostedTower level "sight" shouldBe defaultLevel + 1
        boostedTower level "damage" shouldBe defaultLevel + 1

      }
    }
  }

}
