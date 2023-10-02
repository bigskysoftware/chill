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

    public ChillJobId() {
        this.uuid = UUID.randomUUID();
    }

    ChillJobId(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChillJobId that = (ChillJobId) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "chilljob:job:" + uuid;
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
        return new ChillJobId(uuid);
    }
}
