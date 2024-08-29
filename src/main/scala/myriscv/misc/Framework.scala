package myriscv.misc

import spinal.core._
import spinal.core.fiber._

import scala.collection._
import scala.reflect._

class Framework extends Area {
  val plugins: mutable.ArrayBuffer[Plugin] = mutable.ArrayBuffer[Plugin]()

  def apply[T: ClassTag]: T = {
    val filtered = plugins.collect { case t: T => t }
    filtered.length match {
      case 0 => throw new Exception(s"Can't find the service ${classTag[T].runtimeClass.getName}")
      case 1 => filtered.head
      case _ => throw new Exception(s"Found multiple instances of ${classTag[T].runtimeClass.getName}")
    }
  }
}

class Plugin(framework: Framework) extends Area {
  framework.plugins += this

  val setups: mutable.ArrayBuffer[Handle[_]] = mutable.ArrayBuffer[Handle[_]]()
  val builds: mutable.ArrayBuffer[Handle[_]] = mutable.ArrayBuffer[Handle[_]]()

  def during: Object {
    def setup[T: ClassTag](body: => T): Handle[T]
    def build[T: ClassTag](body: => T): Handle[T]
  } = new {
    def setup[T: ClassTag](body: => T): Handle[T] = {
      val handle = Fiber setup body
      setups += handle
      handle
    }
    def build[T: ClassTag](body: => T): Handle[T] = {
      val handle = Fiber build body
      builds += handle
      handle
    }
  }
}