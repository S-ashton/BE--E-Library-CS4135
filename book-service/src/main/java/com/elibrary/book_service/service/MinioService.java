package com.elibrary.book_service.service;

import com.elibrary.book_service.config.MinioProperties;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties props;

    public MinioService(MinioProperties props) {
        this.props = props;
        this.minioClient = MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
    }

    @PostConstruct
    public void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(props.getBucket()).build());
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(props.getBucket()).build());
            // Set bucket to public-read so React can fetch images directly
            String policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [{
                        "Effect": "Allow",
                        "Principal": {"AWS": ["*"]},
                        "Action": ["s3:GetObject"],
                        "Resource": ["arn:aws:s3:::%s/*"]
                      }]
                    }
                    """.formatted(props.getBucket());
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(props.getBucket())
                            .config(policy)
                            .build());
        }
    }

    /**
     * Uploads a cover image to MinIO and returns the public URL.
     * Returns null if no file is provided.
     */
    public String uploadCoverImage(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String objectName = "covers/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(props.getBucket())
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

        // Return a permanent public URL (bucket is public-read)
        return props.getEndpoint() + "/" + props.getBucket() + "/" + objectName;
    }
}
