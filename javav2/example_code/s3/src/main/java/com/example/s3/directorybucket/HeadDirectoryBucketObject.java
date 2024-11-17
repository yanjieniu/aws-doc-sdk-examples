// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.example.s3.directorybucket;

// snippet-start:[s3directorybuckets.java2.directory_bucket_head_object.import]
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static com.example.s3.util.S3DirectoryBucketUtils.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// snippet-end:[s3directorybuckets.java2.directory_bucket_head_object.import]

/**
 * Before running this example:
 * <p/>
 * The SDK must be able to authenticate AWS requests on your behalf. If you have
 * not configured
 * authentication for SDKs and tools, see
 * https://docs.aws.amazon.com/sdkref/latest/guide/access.html in the AWS SDKs
 * and Tools Reference Guide.
 * <p/>
 * You must have a runtime environment configured with the Java SDK.
 * See
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/setup.html in
 * the Developer Guide if this is not set up.
 * <p/>
 * To use S3 directory buckets, configure a gateway VPC endpoint. This is the
 * recommended method to enable directory bucket traffic without
 * requiring an internet gateway or NAT device. For more information on
 * configuring VPC gateway endpoints, visit
 * https://docs.aws.amazon.com/AmazonS3/latest/userguide/s3-express-networking.html#s3-express-networking-vpc-gateway.
 * <p/>
 * Directory buckets are available in specific AWS Regions and Zones. For
 * details on Regions and Zones supporting directory buckets, see
 * https://docs.aws.amazon.com/AmazonS3/latest/userguide/s3-express-networking.html#s3-express-endpoints.
 */

public class HeadDirectoryBucketObject {
    private static final Logger logger = LoggerFactory.getLogger(HeadDirectoryBucketObject.class);

    // snippet-start:[s3directorybuckets.java2.directory_bucket_head_object.main]
    /**
     * Retrieves metadata for an object in the specified S3 directory bucket.
     *
     * @param s3Client   The S3 client used to interact with S3
     * @param bucketName The name of the directory bucket
     * @param objectKey  The key (name) of the object to retrieve metadata for
     * @return True if the object exists, false otherwise
     */
    public static boolean headDirectoryBucketObject(S3Client s3Client, String bucketName, String objectKey) {
        logger.info("Retrieving metadata for object: {} from bucket: {}", objectKey, bucketName);

        try {
            // Create a HeadObjectRequest
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // Retrieve the object metadata
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            logger.info("Amazon S3 object: \"{}\" found in bucket: \"{}\" with ETag: \"{}\"", objectKey, bucketName,
                    response.eTag());
            logger.info("Content-Type: {}", response.contentType());
            logger.info("Content-Length: {}", response.contentLength());
            logger.info("Last Modified: {}", response.lastModified());
            return true;

        } catch (S3Exception e) {
            logger.error("Failed to retrieve object metadata: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                    e.awsErrorDetails().errorCode());
            return false;
        }
    }
    // snippet-end:[s3directorybuckets.java2.directory_bucket_head_object.main]

    // Main method for testing
    public static void main(String[] args) {
        Region region = Region.US_WEST_2;
        S3Client s3Client = createS3Client(region);
        String zone = "usw2-az1";
        String bucketName = "test-bucket-" + System.currentTimeMillis() + "--" + zone + "--x-s3";
        String objectKey = "example-object-2"; // your-object-key
        Path filePath = Paths.get("src/main/resources/directoryBucket/sample1.txt"); // path to your file
        try {
            // Create the directory bucket
            createDirectoryBucket(s3Client, bucketName, zone);
            // Put an object in the bucket
            putDirectoryBucketObject(s3Client, bucketName, objectKey, filePath);
            // Check the object metadata in the directory bucket

            // Check the object metadata in the directory bucket
            headDirectoryBucketObject(s3Client, bucketName, objectKey);
            boolean objectExists = headDirectoryBucketObject(s3Client, bucketName, objectKey);
            if (objectExists) {
                logger.info("Object metadata retrieved successfully.");
            } else {
                logger.error("Failed to retrieve object metadata.");
            }
        } catch (S3Exception e) {
            logger.error("An error occurred during S3 operations: {} - Error code: {}",
                    e.awsErrorDetails().errorMessage(), e.awsErrorDetails().errorCode());
        } finally {
            try {
                // Delete all objects in the bucket
                logger.info("Deleting the objects in bucket: {}", bucketName);
                deleteAllObjectsInDirectoryBucket(s3Client, bucketName);
                // Tear down by deleting the bucket after testing
                logger.info("Attempting to delete bucket: {}", bucketName);
                deleteDirectoryBucket(s3Client, bucketName);
            } catch (S3Exception e) {
                logger.error("Failed to delete bucket: {} - Error code: {}", e.awsErrorDetails().errorMessage(),
                        e.awsErrorDetails().errorCode());
            } catch (Exception e) {
                logger.error("Failed to delete the bucket due to unexpected error: {}", e.getMessage());
            } finally {
                s3Client.close();
            }
        }
    }
}
