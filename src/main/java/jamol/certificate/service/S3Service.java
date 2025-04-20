package jamol.certificate.service;

import com.amazonaws.services.s3.AmazonS3;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jamol.certificate.entity.Certificate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    @Value("${services.s3.bucket-name}")
    private String bucketName;

    private final AmazonS3 s3Client;


    public String uploadFile(byte[] byteCode, String folder, String uId) {

        InputStream inputStream = new ByteArrayInputStream(byteCode);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(byteCode.length);
        objectMetadata.setContentType("pdf");

        String key = String.format("%s/%s.%s", folder, uId, "pdf");
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName, key, inputStream, objectMetadata
            );
            s3Client.putObject(putObjectRequest);
        } catch (Exception e) {
            log.error("Save error : {}", e.getMessage());
            throw new RuntimeException("Save error !");
        }

        return key;
    }

}


