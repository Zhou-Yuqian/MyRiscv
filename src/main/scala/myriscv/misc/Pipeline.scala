package myriscv.misc

import myriscv._

import spinal.core._
import spinal.core.fiber._

import scala.language.{postfixOps, reflectiveCalls}
import scala.collection._

class Payload[T <: Data](PayloadType: => T) extends HardType[T](PayloadType) with Nameable

class PayloadBundle extends mutable.HashMap[Payload[_ <: Data], Data] with Nameable {
  def apply[T <: Data](payload: Payload[T]): T = {
    getOrElseUpdate(
      key = payload,
      defaultValue = payload()
    ).asInstanceOf[T]
  }

  def top[T <: Data](payload: Payload[T]): T = {
    getOrElseUpdate(
      key = payload,
      defaultValue = ContextSwapper.outsideCondScopeData(payload()).setPartialName(this, payload.getPartialName())
    ).asInstanceOf[T]
  }
}

class Stage extends Area {
  val payloads = new PayloadBundle
  val imports = new PayloadBundle
  val assigns = new PayloadBundle
  val signals = new PayloadBundle
  val exports = new PayloadBundle

  def apply[T <: Data](payload: Payload[T]): Object {
    def default: T
    def assign: T
    def signal: T
  } = new {
    def default: T = imports(payload)
    def assign: T = assigns(payload)
    def signal: T = signals(payload)
  }

  def default[T <: Data](payload: Payload[T]): T = imports(payload)
  def assign[T <: Data](payload: Payload[T]): T = assigns(payload)
  def signal[T <: Data](payload: Payload[T]): T = signals(payload)

  def build(): Unit = {
    payloads.setPartialName("")
    (imports.keys ++ assigns.keys ++ signals.keys ++ exports.keys)
      .toArray.distinct.sortBy(_.getPartialName()).reverse.foreach(payloads.top(_))
    imports.keys.foreach { payload =>
      if(!assigns.contains(payload)) payloads(payload) assignFrom imports(payload)
    }
    assigns.keys.foreach { payload =>
      payloads(payload) assignFrom assigns(payload)
    }
    signals.keys.foreach { payload =>
      signals(payload) assignFrom payloads(payload)
    }
    exports.keys.foreach { payload =>
      exports(payload) assignFrom payloads(payload)
    }
  }
}

case class Bridge(master: Stage, slave: Stage) {
  def build(): Unit = {
    (slave.signals.keys ++ slave.exports.keys).foreach { payload =>
      if(!slave.assigns.contains(payload)) slave.imports(payload)
    }
    slave.imports.keys.foreach { payload =>
      slave.imports(payload) assignFrom RegNext(master.exports(payload), init = payload().clearAll())
    }
  }
}

class Pipeline(config: MyConfig, framework: Framework) extends Plugin(framework) {

  import config._

  val FetchStage = new Stage
  val DecodeStage = new Stage
  val ExecuteStage = new Stage
  val MemoryStage = new Stage
  val WritebackStage = new Stage
  val stages: Array[Stage] = Array(FetchStage, DecodeStage, ExecuteStage, MemoryStage, WritebackStage)

  val bridges = (stages.dropRight(1) zip stages.drop(1)).map(neighbor => Bridge(neighbor._1, neighbor._2))

  val ProgramCounter = new Payload(UInt(xlen bits))
  val Instruction = new Payload(Bits(ilen bits))

  val build = Fiber check new Area {
    bridges.reverse.foreach(_.build())
    stages.reverse.foreach(_.build())
  }

}