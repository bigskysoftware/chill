package chill.storage;

import chill.utils.NiceList;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static chill.utils.TheMissingUtils.safely;

public class LocalStorageProvider implements StorageProvider {
    public static final String LOCAL_STORAGE = "./local_storage/";
    @Override
    public void createBucket(String name) {
        Path path = Path.of(LOCAL_STORAGE + name);
        safely(() -> Files.createDirectory(path));
    }

    @Override
    public void putObject(String bucketName, String fileName, String content) {
        Path path = Path.of(LOCAL_STORAGE + bucketName + "/" + fileName);
        safely(() -> Files.write(path, content.getBytes()));
    }

    @Override
    public void putObject(String bucketName, String fileName, ByteArrayInputStream content, String extension) {
        Path path = Path.of(LOCAL_STORAGE + bucketName + "/" + fileName);
        safely(() -> Files.copy(content, path));
    }

    @Override
    public byte[] getObject(String bucketName, String fileName) {
        Path path = Path.of(LOCAL_STORAGE + bucketName + "/" + fileName);
        return safely(() -> Files.readAllBytes(path));
    }

    @Override
    public NiceList<String> listObjects(String bucketName) {
        Path path = Path.of(LOCAL_STORAGE + bucketName );
        return new NiceList<>(safely(() -> Files.walk(path).toList())).map(p -> p.getFileName().toString());
    }
}
