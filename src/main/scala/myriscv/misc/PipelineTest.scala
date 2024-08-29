package myriscv.misc

import spinal.core._

import scala.language.postfixOps

class PipelineTest()
                  (implicit val framework: Framework) extends Plugin(framework) {

  val logic = during build new Area {
    val pipeline = framework[Pipeline]
    import pipeline._

    val test = Array.fill(4)(Bits(4 bits))
    test(0) := B"0001"
    FetchStage(TestPayload) := test(0)
    test(1) := DecodeStage(TestPayload)
    test(2) := ExecuteStage(TestPayload)
    test(3) := MemoryStage(TestPayload)
  }

}