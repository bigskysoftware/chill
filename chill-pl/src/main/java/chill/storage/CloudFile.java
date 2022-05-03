package chill.storage;


import chill.utils.NiceList;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static chill.storage.Bucket.PATH_SEPARATOR;

public class CloudFile {

    private final Bucket bucket;
    private final String name;

    public CloudFile(Bucket bucket, String fileName) {
        this.bucket = bucket;
        this.name = fileName;
    }

    public void write(String content) {
        bucket.saveFile(this, content);
    }

    public void write(ByteArrayInputStream bytes) {
        bucket.saveFile(this, bytes);
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        int lastDot = name.lastIndexOf(".");
        if (lastDot >= 0) {
            return name.substring(lastDot);
        } else {
            return null;
        }
    }

    public String getContentAsString() {
        byte[] contentFor = bucket.getContentFor(this);
        return new String(contentFor, StandardCharsets.UTF_8);
    }

    public CloudFile getFile(String... folders) {
        String fullPath = new NiceList<String>(folders).join(PATH_SEPARATOR);
        return new CloudFile(bucket, name + PATH_SEPARATOR + fullPath);
    }

    public NiceList<CloudFile> list() {
        var objects = bucket.listObjects(name);
        var filtered = objects.filter(s3ObjectSummary -> isDirectDescendent(s3ObjectSummary.getKey()));
        var cloudFiles = filtered.map(obj -> new CloudFile(bucket, obj.getKey()));
        return cloudFiles;
    }

    private boolean isDirectDescendent(String key) {
        var subPath = key.substring(name.length() + 1);
        boolean containsSlash = subPath.contains(PATH_SEPARATOR);
        return !containsSlash;
    }

    public NiceList<CloudFile> listAll() {
        var objects = bucket.listObjects(name);
        var cloudFiles = objects.map(obj -> new CloudFile(bucket, obj.getKey()));
        return cloudFiles;
    }

    public boolean isDir() {
        return !getShortName().contains(".");
    }

    public String getShortName() {
        int lastSlash = name.lastIndexOf(PATH_SEPARATOR);
        if (lastSlash > 0) {
            return name.substring(lastSlash + 1);
        } else {
            return name;
        }
    }

}
