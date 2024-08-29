package myriscv.misc

import spinal.core._

import scala.language.{postfixOps, reflectiveCalls}
import scala.collection._

class Payload[T <: BaseType](PayloadType: => T) extends HardType[T](PayloadType) with Nameable

class PayloadBundle extends mutable.HashMap[Payload[_ <: BaseType], BaseType] with Nameable {
  def apply[T <: BaseType](payload: Payload[T]): T = {
    getOrElseUpdate(
      key = payload,
      defaultValue = ContextSwapper.outsideCondScopeData(payload())
    ).asInstanceOf[T]
  }
}

class Stage extends Area {
  val defaults = new PayloadBundle
  val payloads = new PayloadBundle

  def apply[T <: BaseType](payload: Payload[T])(implicit bundle: PayloadBundle = payloads): T = bundle(payload)

  def build(): Unit = {
    payloads.keys.foreach { payload =>
      if (!payloads(payload).hasDataAssignment) payloads(payload) assignFrom defaults(payload)
    }
  }

  def display(): Unit = {
    payloads.setPartialName(this, "")
    payloads.keys.foreach { payload =>
      payloads(payload).setPartialName(payloads, payload.getPartialName())
    }
  }
}

class StageLink(master: Stage, slave: Stage) {
  val registers = new mutable.HashMap[Payload[_ <: BaseType], BaseType] with Nameable {
    def apply[T <: BaseType](payload: Payload[T]): T = {
      val register = Reg(payload())
      register initFrom B(0)
      getOrElseUpdate(key = payload, defaultValue = register).asInstanceOf[T]
    }
  }

  def build(): Unit = {
    slave.defaults.keys.foreach { payload =>
      slave.defaults(payload) assignFrom registers(payload)
      if (master.payloads.contains(payload)) {
        registers(payload) assignFrom master.payloads(payload)
      } else {
        registers(payload) assignFrom master.payloads(payload)
        master.payloads(payload) assignFrom master.defaults(payload)
      }
    }
  }
}

class Pipeline()
              (implicit val framework: Framework) extends Plugin(framework) {

  val FetchStage = new Stage
  val DecodeStage = new Stage
  val ExecuteStage = new Stage
  val MemoryStage = new Stage
  val WritebackStage = new Stage

  val TestPayload = new Payload(Bits(4 bits))

  val build = during build new Area {
    framework.plugins.filter(!_.isInstanceOf[Pipeline]).foreach { plugin =>
      plugin.setups.foreach(_.await())
      plugin.builds.foreach(_.await())
    }

    val stages: Array[Stage] = Array(FetchStage, DecodeStage, ExecuteStage, MemoryStage, WritebackStage)
    val links: Array[StageLink] = stages.dropRight(1).zip(stages.drop(1)).map(ms => new StageLink(ms._1, ms._2))
    stages.reverse.foreach(_.build())
    links.reverse.foreach(_.build())
    stages.foreach(_.display())
  }

}