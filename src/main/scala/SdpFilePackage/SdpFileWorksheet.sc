import java.net.InetAddress
import SdpFilePackage.SdpFile

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
sdpFile.writeSdpToFile()
sdpFile.toString
