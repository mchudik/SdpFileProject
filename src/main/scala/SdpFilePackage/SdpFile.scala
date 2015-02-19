package SdpFilePackage

import java.net.{Inet6Address, InetAddress}
import org.joda.time.{DateTimeZone, DateTime}

/**
 * Created by mchudik on 2/16/2015.
 */
class SdpFile {
  def SayHello(name: String) = s"Hello, $name!"
  def getRtpMediaString: String = {
    _rtpMedia.toString()
  }
  val SECONDS_DIFF_NTP_EPOCH_AND_JAVA_EPOCH = 2208988800L
  private val PAYLOAD_TYPE_FOR_AUDIO = 96
  private val PAYLOAD_TYPE_FOR_VIDEO = 97

  private var _modified: Boolean = _
  private var _version: Int = _
  private var _fileName: String = _
  private var _sessionVersionIndex: Int = _
  private val _originator: Originator = new Originator()
  private var _sessionName: String = _
  private var _sessionDescription: String = _
  private val _connectionData: ConnectionData = new ConnectionData()
  private var _startTime: DateTime = _
  private var _endTime: DateTime = _
//  private val _sdpMediaList: List[SdpMedia] = new ArrayList[SdpMedia]
//  private var _previousSdpMediaList: List[SdpMedia] = _
  private var _rtpMedia: RtpMedia = _

  def CRLF = "\r\n"

  def toBytes(xs: Int*) = xs.map(_.toByte).toArray

  def getVersion: Int = _version

  def setVersion(version: Int) {
    updateModifiedVal(_version, version)
    _version = version
  }

  def getFileName: String = _fileName

  def setFileName(fileName: String) {
    updateModified(_fileName, fileName)
    _fileName = fileName
    setSessionVersionIndexFromFileName()
  }

  def getSessionIdentifier: Long = _originator._sessionIdentifier

  def setSessionIdentifier(sessionIdentifier: Long) {
    updateModifiedVal(_originator._sessionIdentifier, sessionIdentifier)
    _originator._sessionIdentifier = sessionIdentifier
  }

  def getSessionVersion: Long = _originator._sessionVersion

  def setSessionVersion(sessionVersion: Long) {
    _originator._sessionVersion = sessionVersion
  }

  def getSessionName: String = _sessionName

  def setSessionName(sessionName: String) {
    updateModified(_sessionName, sessionName)
    _sessionName = sessionName
  }

  def getSessionDescription: String = _sessionDescription

  def setSessionDescription(sessionDescription: String) {
    updateModified(_sessionDescription, sessionDescription)
    _sessionDescription = sessionDescription
  }

  def getStartTime: DateTime = _startTime

  def setStartTime(startTime: DateTime) {
    updateModified(_startTime, normalizeTime(startTime))
    _startTime = startTime
  }

  def getEndTime: DateTime = _endTime

  def setEndTime(endTime: DateTime) {
    updateModified(_endTime, normalizeTime(endTime))
    _endTime = endTime
  }

  def getConnectionAddress: InetAddress = _connectionData._address

  def setConnectionAddress(connectionAddress: InetAddress) {
    updateModified(_connectionData._address, connectionAddress)
    _connectionData._address = connectionAddress
  }

  def addOrUpdateMpeg4AudioMedia(port: Int, sampleRate: Int, channels: Int) {
//    initializeForUpdatingMedia()
    _rtpMedia = findExistingOrCreateRtpMedia(MediaType.audio,  port)
    _rtpMedia._encoding = RtpMedia.Encoding.MPEG4_GENERIC
    _rtpMedia._clockRate = sampleRate
    _rtpMedia._audioChannels = channels
    _rtpMedia._encodingParams = 2
    _rtpMedia._profileLevelId = 2
    _rtpMedia._mode = RtpMedia.Mode.AAC_HBR
    _rtpMedia._sizeLength = 13
    _rtpMedia._indexLength = 3
    _rtpMedia._indexDeltaLength = 3
  }

  def addOrUpdateH264VideoMedia(port: Int) {
//    initializeForUpdatingMedia()
    _rtpMedia = findExistingOrCreateRtpMedia(MediaType.video, port)
    _rtpMedia._encoding = RtpMedia.Encoding.H264
    _rtpMedia._clockRate = 90000
    _rtpMedia._packetizationMode = 1
  }

  private def normalizeTime(dateTime: DateTime): DateTime = {
    if (dateTime == null) {
      return dateTime
    }
      dateTime.withZone(DateTimeZone.UTC).withMillisOfSecond(0)
  }

  private def setSessionVersionIndexFromFileName() {
    if (_fileName != null) {
      val pos = _fileName.lastIndexOf(".")
      if (pos > 0) {
        try {
          _sessionVersionIndex = java.lang.Integer.parseInt(_fileName.substring(0, pos))
        } catch {
          case e: Exception =>
        }
      }
    }
  }

  private def findExistingOrCreateRtpMedia(mediaType: MediaType, port: Int): RtpMedia = {
/*
    var result = findSdpMediaInList(_sdpMediaList, mediaType, port, Protocol.RTP_AVP).asInstanceOf[RtpMedia]
    if (result == null) {
      result = findSdpMediaInList(_previousSdpMediaList, mediaType, port, Protocol.RTP_AVP).asInstanceOf[RtpMedia]
      if (result != null) {
        _sdpMediaList.add(result)
      }
    }
*/
//    if (result == null) {
      val result = SdpMedia.createInstance(mediaType, port, Protocol.RTP_AVP).asInstanceOf[RtpMedia]
      val strMedia = mediaType.getStringRepresentation

      strMedia match {
        case "audio" => result._payloadType = PAYLOAD_TYPE_FOR_AUDIO
        case "video" => result._payloadType = PAYLOAD_TYPE_FOR_VIDEO
      }
//      _sdpMediaList.add(result)
//    }
      result
  }
/*
  private def findSdpMediaInList(sdpMediaList: List[SdpMedia],
                                 mediaType: MediaType,
                                 port: Int,
                                 protocol: Protocol): SdpMedia = {
    var result: SdpMedia = null
    if (sdpMediaList != null) {
      for (existingSdpMedia <- sdpMediaList if existingSdpMedia._port == port && existingSdpMedia._mediaType == mediaType &&
        existingSdpMedia._protocol == protocol) {
        result = existingSdpMedia
      }
    }
    result
  }

  private def initializeForUpdatingMedia() {
    if (_previousSdpMediaList == null) {
      _previousSdpMediaList = new ArrayList[SdpMedia]()
      if (!_sdpMediaList.isEmpty) {
        _previousSdpMediaList.addAll(_sdpMediaList)
        _sdpMediaList.clear()
      }
    }
  }
*/
  override def toString: String = {
    if (_modified || _originator._sessionVersion == 0) {
      _originator._sessionVersion = convertToNtpTimeStamp(new DateTime()) + _sessionVersionIndex
    }
    val sb = new StringBuilder()
    sb.append("v=").append(0).append(CRLF)
    sb.append("o=").append(_originator).append(CRLF)
    sb.append("s=").append(_sessionName).append(CRLF)
    if (_sessionDescription != null && !_sessionDescription.trim().isEmpty) {
      sb.append("i=").append(_sessionDescription.trim()).append(CRLF)
    }
    sb.append("c=").append(_connectionData.toString()).append(CRLF)
    sb.append("t=").append(convertToNtpTimeStamp(_startTime))
      .append(" ")
      .append(convertToNtpTimeStamp(_endTime))
      .append(CRLF)
//    for (sdpMedia <- _sdpMediaList) {
//      sb.append(sdpMedia.toString)
//    }
    sb.toString()
  }

  def isModified: Boolean = {
    if (_modified) {
      return true
    }
/*
    for (sdpMedia <- _sdpMediaList if sdpMedia.isModified) {
      return true
    }
    if (_previousSdpMediaList != null && !_previousSdpMediaList.isEmpty &&
      _previousSdpMediaList.size != _sdpMediaList.size) {
      return true
    }
*/
    false
  }

  def resetModified() {
    _modified = false
/*
    _previousSdpMediaList = null
    for (sdpMedia <- _sdpMediaList) {
      sdpMedia.resetModified()
    }
*/
  }

  protected def updateModified(oldValue: AnyRef, newValue: AnyRef) {
    if (isModified(oldValue, newValue)) {
      _modified = true
    }
  }

  protected def updateModifiedVal(oldValue: AnyVal, newValue: AnyVal) {
    if (isModifiedVal(oldValue, newValue)) {
      _modified = true
    }
  }

  private def isModified(oldValue: AnyRef, newValue: AnyRef): Boolean = {
    if (oldValue == null) {
      newValue != null
    } else {
      oldValue != newValue
    }
  }

  private def isModifiedVal(oldValue: AnyVal, newValue: AnyVal): Boolean = {
      oldValue != newValue
  }

  private def convertToNtpTimeStamp(dateTime: DateTime): Long = {
    if (dateTime == null) 0 else dateTime.getMillis / 1000 + SECONDS_DIFF_NTP_EPOCH_AND_JAVA_EPOCH
  }

  object NetType extends Enumeration {
    val IN = new NetType("IN")

    implicit def convertValue(v: Value): NetType = v.asInstanceOf[NetType]
  }
  class NetType private(private var _stringRepresentation: String) {

    def getStringRepresentation: String = _stringRepresentation
  }

  object AddrType extends Enumeration {
    val IP4 = new AddrType("IP4")
    val IP6 = new AddrType("IP6")

    implicit def convertValue(v: Value): AddrType = v.asInstanceOf[AddrType]
  }
  class AddrType private(private var _stringRepresentation: String) {

    def getStringRepresentation: String = _stringRepresentation
  }

  private class Originator {
    var _userName: String = "-"
    var _sessionIdentifier: Long = convertToNtpTimeStamp(new DateTime())
    var _sessionVersion: Long = _
    var _netType: NetType = NetType.IN
    var _addrType: AddrType = AddrType.IP4
    var _unicastAddress: InetAddress = _
    try {
      _unicastAddress = InetAddress.getByAddress(toBytes(127, 0, 0, 1))
    } catch {
      case e: Exception =>
    }

    override def toString: String = {
      new StringBuilder().append(_userName).append(" ").append(_sessionIdentifier)
        .append(" ")
        .append(_sessionVersion)
        .append(" ")
        .append(_netType.getStringRepresentation)
        .append(" ")
        .append(_addrType.getStringRepresentation)
        .append(" ")
        .append(_unicastAddress.getHostAddress)
        .toString()
    }
  }

  private class ConnectionData {
    var _netType: NetType = NetType.IN
    var _addrType: AddrType = _
    var _address: InetAddress = _
    var _ttl: java.lang.Integer = _

    private def initializeFields() {
      if (_address != null) {
        if (_address.isInstanceOf[Inet6Address]) {
          _addrType = AddrType.IP6
        } else {
          _addrType = AddrType.IP4
          if (_address.isMulticastAddress) {
            _ttl = 0
          }
        }
      }
    }

    override def toString: String = {
      initializeFields()
      val sb = new StringBuilder()
      if (_address != null) {
        sb.append(_netType.getStringRepresentation).append(" ").append(_addrType.getStringRepresentation)
        .append(" ")
        .append(_address.getHostAddress)
        if (_ttl != null) {
          sb.append("/").append(_ttl)
        }
      }
      if (sb.length == 0) {
        null
      } else {
        sb.toString()
      }
    }
  }

  private object SdpMedia {
    def createInstance(mediaType: MediaType, port: Int, protocol: Protocol): SdpMedia = {
      if (protocol == Protocol.RTP_AVP) {
        return new RtpMedia(mediaType, port, protocol)
      }
      throw new RuntimeException("no concrete type known for protocol '" + protocol + "'")
    }
  }

  private abstract class SdpMedia (protected val _mediaType: MediaType, protected val _port: Int, protected val _protocol: Protocol) {
    private var _modified: Boolean = true

    def isModified: Boolean = _modified

    def resetModified() {
      _modified = false
    }

    protected def updateModified(oldValue: AnyRef, newValue: AnyRef) {
      if (isModified(oldValue, newValue)) {
        _modified = true
      }
    }

    private def isModified(oldValue: AnyRef, newValue: AnyRef): Boolean = {
      if (oldValue == null) {
        newValue != null
      } else {
        oldValue != newValue
      }
    }
  }

  private object RtpMedia {

    object Encoding extends Enumeration {
      val H264 = new Encoding("H264")
      val MPEG4_GENERIC = new Encoding("MPEG4-GENERIC")

      implicit def convertValue(v: Value): Encoding = v.asInstanceOf[Encoding]
    }

    object Mode extends Enumeration {
      val AAC_HBR = new Mode("AAC-hbr")

      implicit def convertValue(v: Value): Mode = v.asInstanceOf[Mode]
    }

  }

  class Encoding (private var _stringRepresentation: String) {

    def getStringRepresentation: String = _stringRepresentation
  }

  class Mode (private var _stringRepresentation: String) {

    def getStringRepresentation: String = _stringRepresentation
  }

  private class RtpMedia (mediaType: MediaType, port: Int, protocol: Protocol)
    extends SdpMedia(mediaType, port, protocol) {
    var _payloadType: Int = _
    var _encoding: Encoding = _
    var _clockRate: Int = _
    var _audioChannels: java.lang.Integer = _
    var _profileLevelId: java.lang.Integer = _
    var _encodingParams: java.lang.Integer = _
    var _mode: Mode = _
    var _packetizationMode: java.lang.Integer = _
    var _sizeLength: java.lang.Integer = _
    var _indexLength: java.lang.Integer = _
    var _indexDeltaLength: java.lang.Integer = _

    if (protocol != Protocol.RTP_AVP) {
      throw new RuntimeException("protocol '" + protocol + "' is invalid for RtpMedia")
    }

    override def toString: String = {
      if (_protocol == null || _encoding == null) {
        return super.toString()
      }
      val sb = new StringBuilder()
      sb.append("m=").append(_mediaType.getStringRepresentation).append(" ").append(_port)
        .append(" ")
        .append(_protocol.getStringRepresentation)
        .append(" ")
        .append(_payloadType)
        .append(CRLF)
      sb.append("a=").append("rtpmap:").append(_payloadType)
        .append(" ")
        .append(_encoding.getStringRepresentation)
        .append("/")
        .append(_clockRate)
      if (_audioChannels != null) {
        sb.append("/").append(_audioChannels)
      }
      sb.append(CRLF)
      val customParameters = determineCustomParameters()
      if (customParameters != null) {
        sb.append("a=").append("fmtp:").append(_payloadType)
          .append(" ")
          .append(customParameters)
          .append(CRLF)
      }
      sb.toString()
    }

    private def determineCustomParameters(): String = {
      val sb = new StringBuilder()
      if (_packetizationMode != null) {
        addSeparator(sb)
        sb.append("packetization-mode=").append(_packetizationMode)
      }
      if (_encodingParams != null) {
        addSeparator(sb)
        sb.append("encoding-params=").append(_encodingParams)
      }
      if (_profileLevelId != null) {
        addSeparator(sb)
        sb.append("profile-level-id=").append(_profileLevelId)
      }
      if (_mode != null) {
        addSeparator(sb)
        sb.append("mode=").append(_mode.getStringRepresentation)
      }
      val config = calculateConfig()
      if (config != null) {
        addSeparator(sb)
        sb.append("config=").append(config)
      }
      if (_sizeLength != null) {
        addSeparator(sb)
        sb.append("sizelength=").append(_sizeLength)
      }
      if (_indexLength != null) {
        addSeparator(sb)
        sb.append("indexlength=").append(_indexLength)
      }
      if (_indexDeltaLength != null) {
        addSeparator(sb)
        sb.append("indexdeltalength=").append(_indexDeltaLength)
      }
      if (sb.length == 0) {
        null
      } else {
        sb.toString()
      }
    }

    private def addSeparator(sb: StringBuilder) {
      if (sb.length > 0) {
        sb.append(";")
      }
    }

    private def calculateConfig(): String = {
      if (_profileLevelId == null || _audioChannels == null) {
        return null
      }
      var binaryValue = _profileLevelId
      binaryValue <<= 4
      binaryValue |= audioFrequencyIndex()
      binaryValue <<= 4
      binaryValue |= _audioChannels
      binaryValue <<= 3
      java.lang.Integer.toHexString(binaryValue)
    }

    private def audioFrequencyIndex(): Int = {
      val audioSampleRate = _clockRate
      if (audioSampleRate == 48000) {
        3
      } else if (audioSampleRate == 44100) {
        4
      } else if (audioSampleRate == 32000) {
        5
      } else if (audioSampleRate == 24000) {
        6
      } else if (audioSampleRate == 22050) {
        7
      } else {
        throw new RuntimeException("sample rate of " + audioSampleRate + " is not supported")
      }
    }
  }

  object MediaType extends Enumeration {
    val audio = new MediaType("audio")
    val video = new MediaType("video")

    implicit def convertValue(v: Value): MediaType = v.asInstanceOf[MediaType]
  }
  class MediaType private(private var _stringRepresentation: String) {

    def getStringRepresentation: String = _stringRepresentation
  }



  object Protocol extends Enumeration {
    val RTP_AVP = new Protocol("RTP/AVP")

    implicit def convertValue(v: Value): Protocol = v.asInstanceOf[Protocol]
  }

  class Protocol private(private var _stringRepresentation: String) {

    def getStringRepresentation: String = _stringRepresentation
  }

}
