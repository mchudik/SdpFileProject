package SdpFilePackage
/**
 * Created by mchudik on 2/16/2015.
 */
class SdpFileTest extends org.scalatest.FunSuite {
  test("SayHello Method works correctly") {
    val sdpFile = new SdpFile
    assert(sdpFile.SayHello("SdpFile") == "Hello, SdpFile!")
  }
  test("setFileName/getFileName Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setFileName("SdpFileName.sdp")
    assert(sdpFile.getFileName == "SdpFileName.sdp")
  }
  test("setSessionName/setSessionName Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setSessionName("Session Name")
    assert(sdpFile.getSessionName == "Session Name")
  }
}
