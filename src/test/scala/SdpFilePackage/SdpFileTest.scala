package SdpFilePackage

import java.net.InetAddress

/**
 * Created by mchudik on 2/16/2015.
 */
class SdpFileTest extends org.scalatest.FunSuite {
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
  test("writeSdpToFile Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setVersion(12345)
    sdpFile.setSessionIdentifier(123456789)
    sdpFile.setSessionVersion(123456789)
    sdpFile.setSessionName("Session Name")
    sdpFile.setSessionDescription("Session Description")
    sdpFile.setFileName("C:\\temp\\audio-video.sdp")
    sdpFile.setConnectionAddress(InetAddress.getByAddress(sdpFile.toBytes(10, 4, 10, 34)))
    sdpFile.addOrUpdateMpeg4AudioMedia(65528, 44100, 2)
    sdpFile.addOrUpdateH264VideoMedia(65532)
    sdpFile.setStartTime(org.joda.time.Instant.now().toDateTime)
    sdpFile.setEndTime(org.joda.time.Instant.now().toDateTime)
    assert(sdpFile.writeSdpToFile())
  }
  test("configureS3/testConnectionToS3 Method works correctly") {
    val sdpFile = new SdpFile
    assert(sdpFile.testConnectionToS3())
  }
  test("uploadSdpToS3 Method works correctly") {
    val sdpFile = new SdpFile
    sdpFile.setVersion(12345)
    sdpFile.setSessionIdentifier(123456789)
    sdpFile.setSessionVersion(123456789)
    sdpFile.setSessionName("Session Name")
    sdpFile.setSessionDescription("Session Description")
    val startTime = org.joda.time.Instant.now().toDateTime
    sdpFile.setStartTime(startTime)
    val endTime = org.joda.time.Instant.now().toDateTime
    sdpFile.setEndTime(endTime)
    sdpFile.setFileName("audio-video1.sdp")
    sdpFile.setConnectionAddress(InetAddress.getByAddress(sdpFile.toBytes(10, 4, 10, 34)))
    sdpFile.addOrUpdateMpeg4AudioMedia(1935, 44100, 2)
    sdpFile.addOrUpdateH264VideoMedia(1935)
    println(sdpFile.toString)
    assert(sdpFile.uploadSdpToS3())
  }
}
