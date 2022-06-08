package chill.storage;

import chill.utils.NiceList;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static chill.utils.TheMissingUtils.nice;
import static chill.utils.TheMissingUtils.safely;

public class CloudBucket {

    public static final String PATH_SEPARATOR = "/";
    private final String name;
    private final CloudStorage storage;

    public CloudBucket(String name, CloudStorage storage) {
        this.name = name;
        this.storage = storage;
    }

    public CloudFile getFile(Path path) {
        return getFile(path.toString());
    }

    public CloudFile getFile(String... path) {
        String strPath = nice(path).join(PATH_SEPARATOR);
        return new CloudFile(this, strPath);
    }

    CloudStorage getStorage() {
        return storage;
    }

    public String getName() {
        return name;
    }

    void saveFile(CloudFile file, String content) {
        StorageProvider provider = storage.getClient();
        provider.createBucket(this.name);
        provider.putObject(this.name, file.getName(), content);
    }
    void saveFile(CloudFile file, ByteArrayInputStream content) {
        StorageProvider client = storage.getClient();
        client.createBucket(this.name);
        client.putObject(this.name, file.getName(), content, file.getExtension());
    }

    public byte[] getContentFor(CloudFile file) {
        StorageProvider client = storage.getClient();
        client.createBucket(this.name);
        return client.getObject(this.name, file.getName());
    }

    public NiceList<String> listObjects(String name) {
        StorageProvider client = getStorage().getClient();
        return client.listObjects(name);
    }
}
