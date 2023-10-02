package chill.job.dashboard;

import chill.db.ChillRecord;
import chill.job.ChillJobId;
import chill.job.ChillJobWorker;
import chill.job.impl.DefaultChillJobWorker;
import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.DriverManager;

public class Dashboard {
    public static final Gson gson = new Gson();

    private static JsonMapper jsonMapper = new JsonMapper() {
        @NotNull
        @Override
        public <T> T fromJsonStream(@NotNull InputStream json, @NotNull Type targetType) {
            return gson.fromJson(new InputStreamReader(json), targetType);
        }

        @NotNull
        @Override
        public String toJsonString(@NotNull Object obj, @NotNull Type type) {
            return gson.toJson(obj, type);
        }
    };

    public static void main(String[] args) {
        ChillRecord.connectionSource = () -> DriverManager.getConnection("jdbc:h2:./chill_job");
        ChillJobWorker.setDefaultInstance(new DefaultChillJobWorker(8));
        var dashboard = new Dashboard();
        dashboard.start(args);
    }

    public void start(String[] args) {
        var app = Javalin.create(config -> {
                    config.staticFiles.add("/dashboard", Location.CLASSPATH);
                    config.jsonMapper(jsonMapper);
                })
                .get("/", ctx -> ctx.redirect("index.html"))
                .get("jobs", this::getJobs)
                .post("jobs/{id}/cancel", this::cancelJob)
                .start(8620);
        try (var ignored = app.start(8620)) {
            System.out.println("sup 8620");
        }
    }

    private void cancelJob(Context context) {
        var id = context.pathParam("id");
        ChillJobId jobId = ChillJobId.fromString(id);
        if (ChillJobWorker.getDefaultInstance().cancelJob(jobId)) {
            context.result("ok");
        } else {
            throw new NotFoundResponse("Could not find job: " + jobId);
        }
    }

    private void getJobs(Context context) {
        context.json("Null");
//        var entities = JobEntity.find.all().toList();
//        context.json(entities);
    }
}
