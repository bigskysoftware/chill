package chill.storage;

import chill.env.ChillEnv;
import chill.utils.ChillLogs;

public class CloudStorage {

    static private ChillLogs.LogCategory LOG = ChillLogs.get(CloudStorage.class);
    private static CloudStorage INSTANCE = new CloudStorage();
    private final StorageProvider storageProvider;

    private CloudStorage() {
        this(inferProvider());
    }

    public CloudStorage(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    private static StorageProvider inferProvider() {
        if (ChillEnv.getS3Region() != null) {
            return new S3StorageProvider();
        } else {
            LOG.warn("No S3 Configuration Detected, using local file system storage");
            return new LocalStorageProvider();
        }
    }

    StorageProvider getClient() {
        return storageProvider;
    }

    public static CloudBucket getBucket(String bucketName) {
        return new CloudBucket(bucketName, INSTANCE);
    }

    public static void main(String[] args) {
        ChillEnv.PORT.setManualValue(8888, "For testing");
        CloudStorage storage = new CloudStorage();
        CloudBucket bucket = storage.getBucket("chillhaus-testdata");
        CloudFile file = bucket.getFile("doh/rey.txt");
        file.write("blah blah blah");
    }
}
