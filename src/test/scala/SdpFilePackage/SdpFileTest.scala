package SdpFilePackage
/**
 * Created by mchudik on 2/16/2015.
 */
class SdpFileTest extends org.scalatest.FunSuite {
  test("SayHello Method works correctly") {
    val sdpFile = new SdpFile
    assert(sdpFile.SayHello("SdpFile") == "Hello, SdpFile!")
  }
}
