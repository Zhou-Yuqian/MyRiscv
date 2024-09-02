package myriscv

import myriscv.misc._
import myriscv.fetch._

import spinal.core._

import scala.language.postfixOps

case class MyConfig(
                     xlen: Int = 32,
                     ilen: Int = 32,
                     resetVector: Long = 0x80000000L
                   )

object MyRiscv extends App {
  SpinalVerilog(new MyRiscv(MyConfig()))
}

class MyRiscv(config: MyConfig) extends Component {
  val framework: Framework = new Framework

  val Pipeline = new Pipeline(config, framework)
  val PipelineTest = new PipelineTest(framework)

  val ProgramCounter = new ProgramCounter(config, framework)
  val InstructionFetchUnit = new InstructionFetchUnit(config, framework)

}