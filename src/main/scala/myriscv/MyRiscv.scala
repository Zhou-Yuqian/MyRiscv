package myriscv

import myriscv.misc._
import spinal.core._

import scala.language.postfixOps

case class MyConfig()

object MyRiscv extends App {
  SpinalVerilog(new MyRiscv(MyConfig()))
}

class MyRiscv(config: MyConfig) extends Component {

  import config._

  implicit val framework: Framework = new Framework

}