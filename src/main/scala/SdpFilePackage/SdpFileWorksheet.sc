import SdpFilePackage.SdpFile

val sdpFile = new SdpFile
println(sdpFile.SayHello("Test"))
//
sdpFile.addOrUpdateMpeg4AudioMedia(1935, 44100, 2)
val strAudio = sdpFile.getRtpMediaString
println(strAudio)
//
sdpFile.addOrUpdateH264VideoMedia(1935)
val strVideo = sdpFile.getRtpMediaString
println(strVideo)
//
val currentTime = org.joda.time.Instant.now().toDateTime
sdpFile.setEndTime(currentTime)
sdpFile.getEndTime
//
sdpFile.setFileName("SdpFileName.sdp")
sdpFile.toString

