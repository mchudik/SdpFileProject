package SdpFilePackage

import java.net.InetAddress

/**
 * Created by mchudik on 2/16/2015.
 */
class SdpFileTest extends org.scalatest.FunSuite {
  test("SayHello Method works correctly") {
    val sdpFile = new SdpFile
    assert(sdpFile.SayHello("SdpFile") == "Hello, SdpFile!")
  }
  test("setVersion/getVersion Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setVersion(12345)
    assert(sdpFile.getVersion == 12345)
  }
  test("setFileName/getFileName Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setFileName("SdpFileName.sdp")
    assert(sdpFile.getFileName == "SdpFileName.sdp")
  }
  test("setSessionIdentifier/getSessionIdentifier Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setSessionIdentifier(123456789)
    assert(sdpFile.getSessionIdentifier == 123456789)
  }
  test("setSessionVersion/getSessionVersion Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setSessionVersion(123456789)
    assert(sdpFile.getSessionVersion == 123456789)
  }
  test("setSessionName/setSessionName Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setSessionName("Session Name")
    assert(sdpFile.getSessionName == "Session Name")
  }
  test("setSessionDescription/getSessionDescription Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setSessionDescription("Session Description")
    assert(sdpFile.getSessionDescription == "Session Description")
  }
  test("setStartTime/getStartTime Method works correctly") {
    val sdpFile = new SdpFile
    val currentTime = org.joda.time.Instant.now().toDateTime
    sdpFile.setStartTime(currentTime)
    assert(sdpFile.getStartTime == currentTime)
  }
  test("setEndTime/getEndTime Method works correctly") {
    val sdpFile = new SdpFile
    val currentTime = org.joda.time.Instant.now().toDateTime
    sdpFile.setEndTime(currentTime)
    assert(sdpFile.getEndTime == currentTime)
  }
  test("setConnectionAddress/setConnectionAddress Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setConnectionAddress(InetAddress.getByAddress(sdpFile.toBytes(127, 0, 0, 1)))
    assert(sdpFile.getConnectionAddress == InetAddress.getByName("127.0.0.1"))
  }
  test("addOrUpdateMpeg4AudioMedia Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.addOrUpdateMpeg4AudioMedia(1935, 44100, 2)
    val str = sdpFile.getRtpMediaString
    println(str)
  }
  test("addOrUpdateH264VideoMedia Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.addOrUpdateH264VideoMedia(1935)
    val str = sdpFile.getRtpMediaString
    println(str)
  }
}
