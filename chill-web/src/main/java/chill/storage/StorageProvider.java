package chill.storage;

import chill.utils.NiceList;
import com.amazonaws.services.s3.model.S3Object;

import java.io.ByteArrayInputStream;

public interface StorageProvider {
    void createBucket(String name);

    void putObject(String bucketName, String fileName, String content);

    void putObject(String bucketName, String fileName, ByteArrayInputStream content, String extension);

    byte[] getObject(String bucketName, String fileName);

    NiceList<String> listObjects(String name);
}
