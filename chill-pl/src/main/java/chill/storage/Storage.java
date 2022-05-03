package chill.storage;

import chill.env.ChillEnv;
import chill.utils.ChillLogs;
import chill.web.WebServer;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;

public class Storage {

    static private ChillLogs.LogCategory LOG = ChillLogs.get(Storage.class);
    private static Storage INSTANCE = new Storage();
    private AmazonS3 client;

    private Storage() {
        this(initS3Client());
    }

    public Storage(AmazonS3 s3Client) {
        client = s3Client;
    }

    private static AmazonS3 initS3Client() {

        if (ChillEnv.getS3Region() != null) {
            BasicAWSCredentials credentials = new BasicAWSCredentials(ChillEnv.getS3AccessKey(),
                    ChillEnv.getS3SecretKey());
            var s3client = AmazonS3ClientBuilder
                    .standard()
                    .withPathStyleAccessEnabled(true)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(ChillEnv.getS3Region())
                    .build();
            return s3client;
        } else {
            LOG.info("No S3 Configuration Detected, starting local file system storage");
            // No cloud storage service defined, use local mock
            var api = new S3Mock.Builder().withPort(ChillEnv.getS3MockPort()).withFileBackend("./fs").build();
            api.start();
            String localS3ServiceURL = "http://localhost:" + ChillEnv.getS3MockPort();
            var endpoint = new AwsClientBuilder.EndpointConfiguration(localS3ServiceURL, "DUMMY");
            var s3client = AmazonS3ClientBuilder
                    .standard()
                    .withPathStyleAccessEnabled(true)
                    .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                    .withEndpointConfiguration(endpoint)
                    .build();
            return s3client;
        }
    }

    AmazonS3 getClient() {
        return client;
    }

    public static Bucket getBucket(String bucketName) {
        return new Bucket(bucketName, INSTANCE);
    }

    public static void main(String[] args) {
        ChillEnv.PORT.setManualValue(8888, "For testing");
        Storage storage = new Storage();
        Bucket bucket = storage.getBucket("chillhaus-testdata");
        CloudFile file = bucket.getFile("doh/rey.txt");
        file.write("blah blah blah");
    }
}
