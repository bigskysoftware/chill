package web;

import chill.web.ChillRoutes;
import workers.SampleJob;

import java.time.LocalDateTime;

import static chill.web.WebServer.Utils.*;

public class Routes extends ChillRoutes {

    @Override
    public void init() {

        get("/", () -> {
            // render the index template
            render("index.html");
        });

        post("/sample-job", () -> {
            // start a sample job
            new SampleJob().run();

            // return a raw string
            raw("Started Job at " + LocalDateTime.now());
        });

    }

}
