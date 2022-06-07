package chill.storage;

import chill.util.MimeTypes;
import chill.utils.NiceList;
import chill.utils.TheMissingUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;

import static chill.utils.TheMissingUtils.nice;
import static chill.utils.TheMissingUtils.safely;

public class Bucket {

    public static final String PATH_SEPARATOR = "/";
    private final String name;
    private final Storage storage;

    public Bucket(String name, Storage storage) {
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

    Storage getStorage() {
        return storage;
    }

    public String getName() {
        return name;
    }

    void saveFile(CloudFile file, String content) {
        AmazonS3 client = storage.getClient();
        client.createBucket(this.name);
        client.putObject(this.name, file.getName(), content);
    }
    void saveFile(CloudFile file, ByteArrayInputStream content) {
        AmazonS3 client = storage.getClient();
        client.createBucket(this.name);
        ObjectMetadata metadata = new ObjectMetadata();
        String mimeType = MimeTypes.get(file.getExtension());
        if (mimeType != null) {
            metadata.setContentType(mimeType);
        }
        client.putObject(this.name, file.getName(), content, metadata);
    }

    public byte[] getContentFor(CloudFile file) {
        AmazonS3 client = storage.getClient();
        client.createBucket(this.name);
        S3Object object = client.getObject(this.name, file.getName());
        S3ObjectInputStream objectContent = object.getObjectContent();
        return safely(() -> objectContent.readAllBytes());
    }

    public NiceList<S3ObjectSummary> listObjects(String name) {
        AmazonS3 client = getStorage().getClient();
        client.createBucket(this.name);
        var objects = client.listObjectsV2(getName(), name);
        return new NiceList<>(objects.getObjectSummaries());
    }
}
