package chill.job;

import java.util.Objects;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * ChillJobId is a unique identifier for a ChillJob.
 * <h3>Format</h3>
 * chilljob:job:[uuid]:[tag]
 */
public final class ChillJobId {
    private final UUID uuid;
    private final String tag;

    public ChillJobId(String tag) {
        this.uuid = UUID.randomUUID();
        this.tag = tag;
    }

    ChillJobId(UUID uuid, String tag) {
        this.uuid = uuid;
        this.tag = tag;
    }

    public ChillJobId() {
        this.uuid = UUID.randomUUID();
        this.tag = "";
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChillJobId that = (ChillJobId) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, tag);
    }

    @Override
    public String toString() {
        return "chilljob:job:" + uuid + ":" + tag;
    }


    public static ChillJobId fromString(String s) throws IllegalArgumentException {
        String[] parts = s.split(":", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid ChillJobId format");
        }
        if (!parts[0].equals("chilljob")) {
            throw new IllegalArgumentException("Invalid ChillJobId format");
        }
        if (!parts[1].equals("job")) {
            throw new IllegalArgumentException("Invalid ChillJobId format");
        }
        UUID uuid = UUID.fromString(parts[2]);
        String tag = parts[3];
        return new ChillJobId(uuid, tag);
    }
}
