package chill.storage;

import chill.env.ChillEnv;
import chill.utils.ChillLogs;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;

public class S3 {

    private static final String MOCK_S3_DIRECTORY = "./s3/";
    public static final int MOCK_S3_PORT = 8001;
    public static final String MOCK_S3_REGION = "us-west-2";
    public static ChillLogs.LogCategory LOG = ChillLogs.get(S3.class);

    public static final boolean MOCKED;
    static {
        MOCKED  = init();
    }

    static boolean init() {
        if (ChillEnv.getS3AccessKey() == null) {
            LOG.info("No S3 is configured, a local storage server will be started");
            S3Mock api = new S3Mock.Builder().withPort(MOCK_S3_PORT).withFileBackend(MOCK_S3_DIRECTORY).build();
            api.start();
            return true;
        }
        return false;
    }

    public static AmazonS3 get() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true);
        if (MOCKED) {
            var endpoint = new EndpointConfiguration("http://localhost:" + MOCK_S3_PORT, MOCK_S3_REGION);
            builder.withEndpointConfiguration(endpoint)
                    .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()));
        } else {
            var credentials = new BasicAWSCredentials(ChillEnv.getS3AccessKey(), ChillEnv.getS3SecretKey());
            builder = builder.withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(ChillEnv.getS3Region());
        }
        return builder.build();
    }

    public static void main(String[] args) {
        var s3 = get();
        s3.createBucket("test");
        s3.putObject("test", "test", "test content");
    }

}
