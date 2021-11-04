package model.stats

import model.stats.Stats.GameStats
import model.stats.StatsTest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

object StatsTest {
  val stats: GameStats = GameStats()
  val amount: Int = 10
}

class StatsTest extends AnyWordSpec with Matchers {

  "The Stats" when {
    "just created" should {
      "have default behaviour" in {
        stats.wallet shouldBe Stats.startingWallet
        stats.life shouldBe Stats.startingLife
        stats.round shouldBe Stats.startingRound
      }
    }
    "modified" should {
      "change its values" in {
        stats pay amount
        stats.wallet shouldBe Stats.startingWallet - amount
        stats gain amount
        stats.wallet shouldBe Stats.startingWallet
        stats lose amount
        stats.life shouldBe Stats.startingLife - amount
        stats.nextRound()
        stats.round shouldBe Stats.startingRound + 1
      }
    }
  }

}
