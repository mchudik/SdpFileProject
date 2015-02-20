package awsUtil

import java.io.File

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.transfer.TransferManager

import scala.util.{Failure, Success, Try}

/**
 * Created by mchudik on 2/19/2015.
 */
trait S3Util {

  protected def createTransferManager(awsAccessKeyId: String, awsSecretAccessKey: String): TransferManager = {

    val transferManager = new TransferManager(new AmazonS3Client(new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)))
    testConnection(transferManager)
    transferManager
  }

  private def testConnection(transferManager: TransferManager): Unit = {

    Try(transferManager.getAmazonS3Client.getS3AccountOwner) match {
      case Failure(e) =>
        throw new RuntimeException(s"unable to establish connection to S3: ${e.getMessage}", e)
      case Success(_) =>
    }
  }

  protected def uploadFileToS3(awsAccessKeyId: String,
                                awsSecretAccessKey: String,
                                bucket: String,
                                key: String,
                                file: File
                                ): Unit = {
    Try {
      val transferManager = new TransferManager(new AmazonS3Client(new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)))
      val myUpload = transferManager.upload(bucket, key, file)
      myUpload.waitForCompletion()
      transferManager.shutdownNow()
    } match {
      case Failure(e) =>
        throw new RuntimeException(s"unable to upload file to S3: ${e.getMessage}", e)
      case Success(_) =>
    }
  }
}
