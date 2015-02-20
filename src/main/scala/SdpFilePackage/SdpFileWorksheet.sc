import java.net.InetAddress

import SdpFilePackage.SdpFile

val sdpFile = new SdpFile
sdpFile.setSessionName("2c3f7112-7fe2-4fd1-ad09-ec4acb6a40fe")
sdpFile.setFileName("C:\\temp\\audio-video.sdp")
sdpFile.setStartTime(org.joda.time.Instant.now().toDateTime)
sdpFile.setEndTime(org.joda.time.Instant.now().toDateTime)
sdpFile.setConnectionAddress(InetAddress.getByAddress(sdpFile.toBytes(10, 4, 10, 34)))
sdpFile.addOrUpdateMpeg4AudioMedia(65528, 44100, 2)
sdpFile.addOrUpdateH264VideoMedia(65532)
sdpFile.writeSdpToFile()
sdpFile.toString
