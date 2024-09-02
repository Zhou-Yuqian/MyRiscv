package myriscv.misc

import spinal.core._
import spinal.core.fiber._

import scala.language.{postfixOps, reflectiveCalls}

class PipelineTest(framework: Framework) extends Plugin(framework) {

  val logic = Fiber build new Area {
    val pipeline = framework[Pipeline]
    import pipeline._

    val inst = Array.fill(5)(Instruction())
    inst(0) := B(11)
    FetchStage assign Instruction := inst(0)
    inst(1) := WritebackStage signal Instruction

    val pc = Array.fill(5)(ProgramCounter())
    pc(0) := U(1)
    FetchStage assign ProgramCounter := pc(0)
    pc(1) := WritebackStage signal ProgramCounter
  }

}