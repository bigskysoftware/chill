import java.util.Objects;

public class ChillJob {
    private Id id;
    private Object payload;
    private final ChillJobManager manager;

    public ChillJob(Object payload) {
        this.payload = Objects.requireNonNull(payload);
        this.manager = GlobalChillJobManager.getInstance();
    }

    public ChillJob(Object payload, ChillJobManager manager) {
        this.payload = Objects.requireNonNull(payload);
        this.manager = Objects.requireNonNull(manager);
    }

    public ChillJob(Id id) {
        this.id = Objects.requireNonNull(id);
        this.manager = GlobalChillJobManager.getInstance();
    }

    public ChillJob(Id id, ChillJobManager manager) {
        this.id = Objects.requireNonNull(id);
        this.manager = Objects.requireNonNull(manager);
    }

    public <T> T getPayload() {
        if (payload == null) {
            fetchPayload();
        }
        return (T) payload;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = Objects.requireNonNull(id);
    }

    public Object fetchPayload() {
        manager.update(this);
        return payload;
    }

    public void ensureHasId() throws AssertionError {
        manager.ensureSubmitted(this);
    }

    public void ensureNoId() throws AssertionError {
        manager.ensureNotSubmitted(this);
    }

    public static class Id {
        private final long id;

        public Id(String inner) {
            if (!inner.startsWith("chilljob:")) {
                throw new IllegalArgumentException("Invalid id: " + inner + ". expected chilljob:<long-id>");
            }
            var parts = inner.split(":");
            if (parts.length == 2) {
                this.id = Long.parseLong(parts[1]);
            } else {
                throw new IllegalArgumentException("Invalid id: " + inner + ". expected chilljob:<long-id>");
            }
        }

        public Id(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return Objects.equals(this.id, id.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "chilljob:" + id;
        }
    }
}
