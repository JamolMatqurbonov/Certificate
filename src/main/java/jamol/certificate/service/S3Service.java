package jamol.certificate.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${services.s3.bucket-name}")
    private String bucketName;

    private final AmazonS3 s3Client;

    /**
     * Faylni S3'ga yuklash
     */
    public String uploadFile(byte[] pdfBytes, String objectKey, String uuid) {
        try {
            InputStream inputStream = new ByteArrayInputStream(pdfBytes);

            // Fayl metadata (ma'lumotlar) ni sozlash
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/pdf");
            metadata.setContentLength(pdfBytes.length);

            // Agar bucket mavjud bo'lmasa, uni yaratish
            if (!s3Client.doesBucketExistV2(bucketName)) {
                s3Client.createBucket(bucketName);
            }

            // Faylni S3'ga yuklash
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName, objectKey, inputStream, metadata
            );
            s3Client.putObject(putObjectRequest);

            log.info("Fayl S3'ga muvaffaqiyatli yuklandi, kalit: {}", objectKey);

            return objectKey; // Faylning S3 kaliti
        } catch (Exception e) {
            log.error("Upload to S3 failed: {}", e.getMessage());
            throw new RuntimeException("Faylni S3'ga yuklashda xato");
        }
    }
}
