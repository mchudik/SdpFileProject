package SdpFilePackage

import java.net.{Inet6Address, UnknownHostException, InetAddress}
import java.util.{ArrayList, List}
import org.scala_tools.time.Imports._
//import scala.collection.JavaConversions.__

/**
 * Created by mchudik on 2/16/2015.
 */
class SdpFile {
  val SECONDS_DIFF_NTP_EPOCH_AND_JAVA_EPOCH = 2208988800L
  private val PAYLOAD_TYPE_FOR_AUDIO = 96
  private val PAYLOAD_TYPE_FOR_VIDEO = 97

  private var _modified: Boolean = _
  private var _version: Int = _
  private var _fileName: String = _
  private var _sessionVersionIndex: Int = _
  private var _originator: Originator = new Originator()
  private var _sessionName: String = _
  private var _sessionDescription: String = _
  private var _connectionData: ConnectionData = new ConnectionData()
  private var _startTime: DateTime = _
  private var _endTime: DateTime = _
  private var _sdpMediaList: List[SdpMedia] = new ArrayList[SdpMedia]()
  private var _previousSdpMediaList: List[SdpMedia] = _

  def CRLF = "\r\n" | "\n"
  def SayHello(name:String) = s"Hello, $name!"

  def setVersion(version: Int) {
    updateModified(_version, version)
    _version = version
  }

  def getFileName: String = _fileName

  def setFileName(fileName: String) {
    updateModified(_fileName, fileName)
    _fileName = fileName
    setSessionVersionIndexFromFileName()
  }

  def setSessionIdentifier(sessionIdentifier: Long) {
    updateModified(_originator._sessionIdentifier, sessionIdentifier)
    _originator._sessionIdentifier = sessionIdentifier
  }

  def setSessionVersion(sessionVersion: Long) {
    _originator._sessionVersion = sessionVersion
  }

  def getSessionName: String = _sessionName

  def setSessionName(sessionName: String) {
    updateModified(_sessionName, sessionName)
    _sessionName = sessionName
  }

  def setSessionDescription(sessionDescription: String) {
    updateModified(_sessionDescription, sessionDescription)
    _sessionDescription = sessionDescription
  }

  def setStartTime(startTime: DateTime) {
    startTime = normalizeTime(startTime)
    updateModified(_startTime, startTime)
    _startTime = startTime
  }

  def setEndTime(endTime: DateTime) {
    endTime = normalizeTime(endTime)
    updateModified(_endTime, endTime)
    _endTime = endTime
  }

  def setConnectionAddress(connectionAddress: InetAddress) {
    updateModified(_connectionData._address, connectionAddress)
    _connectionData._address = connectionAddress
  }

  def addOrUpdateMpeg4AudioMedia(port: Int, sampleRate: Int, channels: Int) {
    initializeForUpdatingMedia()
    val rtpMedia = findExistingOrCreateRtpMedia(MediaType.audio, port)
    rtpMedia._encoding = RtpMedia.Encoding.MPEG4_GENERIC
    rtpMedia._clockRate = sampleRate
    rtpMedia._audioChannels = channels
    rtpMedia._encodingParams = 2
    rtpMedia._profileLevelId = 2
    rtpMedia._mode = RtpMedia.Mode.AAC_HBR
    rtpMedia._sizeLength = 13
    rtpMedia._indexLength = 3
    rtpMedia._indexDeltaLength = 3
  }

  def addOrUpdateH264VideoMedia(port: Int) {
    initializeForUpdatingMedia()
    val rtpMedia = findExistingOrCreateRtpMedia(MediaType.video, port)
    rtpMedia._encoding = RtpMedia.Encoding.H264
    rtpMedia._clockRate = 90000
    rtpMedia._packetizationMode = 1
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
    var result = findSdpMediaInList(_sdpMediaList, mediaType, port, Protocol.RTP_AVP).asInstanceOf[RtpMedia]
    if (result == null) {
      result = findSdpMediaInList(_previousSdpMediaList, mediaType, port, Protocol.RTP_AVP).asInstanceOf[RtpMedia]
      if (result != null) {
        _sdpMediaList.add(result)
      }
    }
    if (result == null) {
      result = SdpMedia.createInstance(mediaType, port, Protocol.RTP_AVP).asInstanceOf[RtpMedia]
      mediaType match {
        case audio => result._payloadType = PAYLOAD_TYPE_FOR_AUDIO
        case video => result._payloadType = PAYLOAD_TYPE_FOR_VIDEO
      }
      _sdpMediaList.add(result)
    }
    result
  }

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

  override def toString: String = {
    if (_modified || _originator._sessionVersion == 0) {
      _originator._sessionVersion = convertToNtpTimeStamp(new DateTime()) + _sessionVersionIndex
    }
    val sb = new StringBuilder()
    sb.append("v=").append(0).append(CRLF)
    sb.append("o=").append(_originator).append(CRLF)
    sb.append("s=").append(sessionName).append(CRLF)
    if (_sessionDescription != null && !_sessionDescription.trim().isEmpty) {
      sb.append("i=").append(_sessionDescription.trim()).append(CRLF)
    }
    sb.append("c=").append(_connectionData.toString).append(CRLF)
    sb.append("t=").append(convertToNtpTimeStamp(_startTime))
      .append(" ")
      .append(convertToNtpTimeStamp(_endTime))
      .append(CRLF)
    for (sdpMedia <- _sdpMediaList) {
      sb.append(sdpMedia.toString)
    }
    sb.toString
  }

  def isModified: Boolean = {
    if (_modified) {
      return true
    }
    for (sdpMedia <- _sdpMediaList if sdpMedia.isModified) {
      return true
    }
    if (_previousSdpMediaList != null && !_previousSdpMediaList.isEmpty &&
      _previousSdpMediaList.size != _sdpMediaList.size) {
      return true
    }
    false
  }

  def resetModified() {
    _modified = false
    _previousSdpMediaList = null
    for (sdpMedia <- _sdpMediaList) {
      sdpMedia.resetModified()
    }
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

  private def convertToNtpTimeStamp(dateTime: DateTime): Long = {
    if (dateTime == null) 0 else dateTime.getMillis / 1000 + SECONDS_DIFF_NTP_EPOCH_AND_JAVA_EPOCH
  }

  object NetType extends Enumeration {
    val IN = new NetType()
    class NetType extends Val

    implicit def convertValue(v: Value): NetType = v.asInstanceOf[NetType]
  }

  object AddrType extends Enumeration {
    val IP4 = new AddrType()
    val IP6 = new AddrType()
    class AddrType extends Val

    implicit def convertValue(v: Value): AddrType = v.asInstanceOf[AddrType]
  }

  private class Originator {
    private var _userName: String = "-"
    private var _sessionIdentifier: Long = convertToNtpTimeStamp(new DateTime())
    private var _sessionVersion: Long = _
    private var _netType: NetType = NetType.IN
    private var _addrType: AddrType = AddrType.IP4
    private var _unicastAddress: InetAddress = _
    try {
      _unicastAddress = InetAddress.getByAddress(Array(127, 0, 0, 1))
    } catch {
      case e: UnknownHostException => ThrowableUtility.rethrow(e)
    }

    override def toString: String = {
      new StringBuilder().append(_userName).append(" ").append(_sessionIdentifier)
        .append(" ")
        .append(_sessionVersion)
        .append(" ")
        .append(_netType)
        .append(" ")
        .append(_addrType)
        .append(" ")
        .append(_unicastAddress.getHostAddress)
        .toString
    }
  }

  private class ConnectionData {
    private var _netType: NetType = NetType.IN
    private var _addrType: AddrType = _
    private var _address: InetAddress = _
    private var _ttl: java.lang.Integer = _
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
      val sb = new StringBuilder().append(_netType).append(" ").append(_addrType)
        .append(" ")
        .append(_address.getHostAddress)
      if (_ttl != null) {
        sb.append("/").append(_ttl)
      }
      sb.toString
    }
  }

  object SdpMedia {
    def createInstance(mediaType: MediaType, port: Int, protocol: Protocol): SdpMedia = {
      if (protocol == Protocol.RTP_AVP) {
        return new RtpMedia(mediaType, port, protocol)
      }
      throw new RuntimeException("no concrete type known for protocol '" + protocol + "'")
    }
  }

  private abstract class SdpMedia private (protected val _mediaType: MediaType, protected val _port: Int, protected val _protocol: Protocol)
  {
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

  object RtpMedia {
    object Encoding extends Enumeration {
      val H264 = new Encoding("H264")
      val MPEG4_GENERIC = new Encoding("MPEG4-GENERIC")
      class Encoding private (private var _stringRepresentation: String) extends Val {

        def getStringRepresentation: String = _stringRepresentation
      }

      implicit def convertValue(v: Value): Encoding = v.asInstanceOf[Encoding]
    }

    object Mode extends Enumeration {
      val AAC_HBR = new Mode("AAC-hbr")
      class Mode private (private var _stringRepresentation: String) extends Val {

        def getStringRepresentation: String = _stringRepresentation
      }

      implicit def convertValue(v: Value): Mode = v.asInstanceOf[Mode]
    }
  }

  private class RtpMedia private (mediaType: MediaType, port: Int, protocol: Protocol)
    extends SdpMedia(mediaType, port, protocol) {
    private var _payloadType: Int = _
    private var _encoding: Encoding = _
    private var _clockRate: Int = _
    private var _audioChannels: java.lang.Integer = _
    private var _profileLevelId: java.lang.Integer = _
    private var _encodingParams: java.lang.Integer = _
    private var _mode: Mode = _
    private var _packetizationMode: java.lang.Integer = _
    private var _sizeLength: java.lang.Integer = _
    private var _indexLength: java.lang.Integer = _
    private var _indexDeltaLength: java.lang.Integer = _

    if (protocol != Protocol.RTP_AVP) {
      throw new RuntimeException("protocol '" + protocol + "' is invalid for RtpMedia")
    }

    override def toString: String = {
      if (_protocol == null || _encoding == null) {
        return super.toString
      }
      val sb = new StringBuilder()
      sb.append("m=").append(_mediaType).append(" ").append(_port)
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
      sb.toString
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
        sb.toString
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
    val audio = new MediaType()
    val video = new MediaType()
    class MediaType extends Val

    implicit def convertValue(v: Value): MediaType = v.asInstanceOf[MediaType]
  }

  object Protocol extends Enumeration {
    val RTP_AVP = new Protocol("RTP/AVP")
    class Protocol private (private var _stringRepresentation: String) extends Val {

      def getStringRepresentation: String = _stringRepresentation
    }

    implicit def convertValue(v: Value): Protocol = v.asInstanceOf[Protocol]
  }
}
