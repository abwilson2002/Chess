package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import java.util.Map;

public class Server {

    private final Javalin javalinObj;

    public Server() {
        javalinObj = Javalin.create(config -> config.staticFiles.add("web"));

        javalinObj.delete("db", ctx -> ctx.result("{}"));

        javalinObj.post("user", ctx -> register(ctx));
        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalinObj.start(desiredPort);
        return javalinObj.port();
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        var res = Map.of("username", req.get("username"), "authToken", "xyz");
        ctx.result(serializer.toJson(res));
    }

    public void stop() {
        javalinObj.stop();
    }
}
