package model.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import controller.Messages.{EntitiesManagerMessage, Update}
import model.actors.BalloonMessages.{BalloonKilled, Hit, Unfreeze}
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{Bullet, Ice}
import model.managers.EntitiesMessages.{EntityUpdated, ExitedBalloon, UpdateEntity}
import model.managers.GameDynamicsMessages.Gain
import utils.Commons

import scala.concurrent.duration.DurationDouble

object BalloonMessages {
  case class Hit(bullet: Bullet, replyTo: ActorRef[Update]) extends Update
  case class BalloonKilled(actorRef: ActorRef[Update]) extends Update with EntitiesManagerMessage
  case object Unfreeze extends Update
}

/**
 * The actor encapsulating a [[Balloon]].
 */
object BalloonActor {

  def apply(balloon: Balloon): Behavior[Update] = Behaviors.setup { ctx =>
    BalloonActor(ctx, balloon).default()
  }
}

/**
 * The [[BalloonActor]] related class, conforming to a common Akka pattern.
 * @param ctx
 *   The actor's context.
 * @param balloon
 *   The encapsulated [[Balloon]].
 */
case class BalloonActor private (
    ctx: ActorContext[Update],
    var balloon: Balloon,
    var hit: Boolean = false) {

  def default(): Behavior[Update] = Behaviors.receiveMessage {
    case UpdateEntity(elapsedTime, _, replyTo) =>
      balloon.position.x match {
        case outOfBounds if outOfBounds >= Commons.View.gameBoardWidth =>
          replyTo ! ExitedBalloon(balloon, ctx.self)
          Behaviors.stopped
        case _ =>
          balloon = balloon.update(elapsedTime).asInstanceOf[Balloon]
          replyTo ! EntityUpdated(balloon, ctx.self)
          Behaviors.same
      }

    case Hit(bullet, replyTo) =>
      hit(bullet, replyTo) {
        case ice: Ice => freeze(ice.freezingTime)
        case _        => Behaviors.same
      }

  }

  def freeze(freezingTime: Double): Behavior[Update] = Behaviors.withTimers { timers =>
    timers.startTimerWithFixedDelay(Unfreeze, freezingTime.seconds)
    frozen()
  }

  def frozen(): Behavior[Update] = Behaviors.withTimers { timers =>
    Behaviors.receiveMessage {
      case Unfreeze =>
        timers.cancel(Unfreeze)
        default()

      case Hit(bullet, replyTo) =>
        hit(bullet, replyTo) { case _ =>
          Behaviors.same
        }

      case UpdateEntity(_, _, replyTo) =>
        replyTo ! EntityUpdated(balloon, ctx.self)
        Behaviors.same

      case _ => Behaviors.same
    }
  }

  private def hit(bullet: Bullet, replyTo: ActorRef[Update])(
      bulletHandler: PartialFunction[Bullet, Behavior[Update]]): Behavior[Update] =
    balloon.pop(bullet) match {
      case None =>
        replyTo ! Gain(10)
        replyTo ! BalloonKilled(ctx.self)
        Behaviors.stopped
      case Some(b) =>
        if (b != balloon) replyTo ! Gain(10)
        balloon = b
        bulletHandler(bullet)
    }
}
