package myriscv.misc

import spinal.core._

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
}