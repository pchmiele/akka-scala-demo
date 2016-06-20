package Demo6

import akka.actor._
import akka.persistence._

case class Cmd(data: String)
case class Evt(data: String)

case class ExampleState(events: List[String] = Nil) {
  def updated(evt: Evt): ExampleState = copy(evt.data :: events)
  def size: Int = events.length
  override def toString: String = events.reverse.toString
}

class ExamplePersistentActor extends PersistentActor with ActorLogging {
  override def persistenceId = "sample-id-1"

  var state = ExampleState()

  def updateState(event: Evt): Unit =
    state = state.updated(event)

  def numEvents =
    state.size

  val receiveRecover: Receive = {
    case evt: Evt => updateState(evt)
    case SnapshotOffer(_, snapshot: ExampleState) => state = snapshot
    case SaveSnapshotSuccess(metadata) => log.info("Snapshot success")
    case SaveSnapshotSuccess => log.info("Snapshot success")
    case SaveSnapshotFailure(metadata, reason) => log.info("Snapshot failure")
    case RecoveryCompleted => log.info("Recovery completed")
  }

  val receiveCommand: Receive = {
    case Cmd(data) =>
      persist(Evt(s"${data}-${numEvents}"))(updateState)
      persist(Evt(s"${data}-${numEvents + 1}")) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
      }
    case "snap"  => saveSnapshot(state)
    case "print" => println(state)
  }
}

object Main extends App {
  val system = ActorSystem("mySystem")
  val persisentActor = system.actorOf(Props[ExamplePersistentActor], "persistentActor")

  persisentActor ! "print"
  persisentActor ! Cmd("some data")
  persisentActor ! "print"
  persisentActor ! "snap"

  system.terminate()

  val system2 = ActorSystem("mySystem")
  val persisentActor2 = system2.actorOf(Props[ExamplePersistentActor], "persistentActor")
  persisentActor2 ! "print"
  system2.terminate()
}