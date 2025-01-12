package com.gauntletai.chat.s3;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@Slf4j
public class S3Service {

    @Value("${aws.s3.bucket}")
    private String bucketName;
    

    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public S3Attachment uploadFile(MultipartFile file) {
        String key = UUID.randomUUID().toString();
        String filename = file.getOriginalFilename();
        
        try {
            s3Client.putObject(request -> request.bucket(bucketName).key(key).contentType(file.getContentType()).contentLength(file.getSize()).build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (AwsServiceException | SdkClientException | IOException e) {
            log.error("Error uploading file to S3 with key: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
        
        return S3Attachment.builder().key(key).filename(filename).build();
    }

    public ResponseEntity<byte[]> getAttachment(S3Attachment attachment) {
        try {
            var getObjectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                .bucket(bucketName)
                .key(attachment.getKey())
                .build();

            var s3Response = s3Client.getObject(getObjectRequest);
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + attachment.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(s3Response.response().contentType()))
                .body(s3Response.readAllBytes());

        } catch (S3Exception e) {
            log.error("Error getting attachment from S3 with key: {}", attachment.getKey(), e);
            throw new RuntimeException("Failed to get attachment from S3", e);
        } catch (IOException e) {
            log.error("Error reading attachment content with key: {}", attachment.getKey(), e);
            throw new RuntimeException("Failed to read attachment content", e);
        }
    }
}
