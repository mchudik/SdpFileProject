import SdpFilePackage.SdpFile

val sdpFile = new SdpFile
println(sdpFile.SayHello("Test"))

sdpFile.addOrUpdateMpeg4AudioMedia(1935, 44100, 2)
val strAudio = sdpFile.getRtpMediaString
println(strAudio)

sdpFile.addOrUpdateH264VideoMedia(1935)
val strVideo = sdpFile.getRtpMediaString
println(strVideo)
