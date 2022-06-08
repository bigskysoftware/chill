package chill.storage;

import chill.env.ChillEnv;
import chill.util.MimeTypes;
import chill.utils.NiceList;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.ByteArrayInputStream;

import static chill.utils.TheMissingUtils.safely;

public class S3StorageProvider implements StorageProvider{
    private AmazonS3 client;

    public S3StorageProvider() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(ChillEnv.getS3AccessKey(),
                ChillEnv.getS3SecretKey());
        client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(ChillEnv.getS3Region())
                .build();
    }

    @Override
    public void createBucket(String name) {
        client.createBucket(name);
    }

    @Override
    public void putObject(String bucketName, String fileName, String content) {
        client.putObject(bucketName, fileName, content);
    }

    @Override
    public void putObject(String bucketName, String fileName, ByteArrayInputStream content, String extension) {
        ObjectMetadata metadata = new ObjectMetadata();
        String mimeType = MimeTypes.get(extension);
        if (mimeType != null) {
            metadata.setContentType(mimeType);
        }
        client.putObject(bucketName, fileName, content, metadata);
    }

    @Override
    public byte[] getObject(String bucketName, String fileName) {
        S3Object object = client.getObject(bucketName, fileName);
        S3ObjectInputStream objectContent = object.getObjectContent();
        return safely(() -> objectContent.readAllBytes());
    }

    @Override
    public NiceList<String> listObjects(String name) {
        client.createBucket(name);
        var objects = client.listObjectsV2(name, name);
        return new NiceList<>(objects.getObjectSummaries()).map(s3ObjectSummary -> s3ObjectSummary.getKey());
    }
}
