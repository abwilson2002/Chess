package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import java.util.Map;
import model.*;
import dataaccess.*;

public class Server {

    private final Javalin javalinObj;

    public Server() {
        javalinObj = Javalin.create(config -> config.staticFiles.add("web"));

        javalinObj.post("user", this::register);

        javalinObj.post("session", this::login);

        javalinObj.delete("session", this::logout);

        javalinObj.get("game", this::list);

        javalinObj.post("game", this::create);

        javalinObj.put("game", this::join);

        javalinObj.delete("db", this::clear);
    }


    //Handler Functions

    public int run(int desiredPort) {
        javalinObj.start(desiredPort);
        return javalinObj.port();
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        var res = Map.of("username", req.get("username"), "authToken", req.get("authToken"));
        ctx.result(serializer.toJson(res));
    }

    private void login(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), UserData.class);

        var res = Map.of("username", req.getUsername(), "authToken", req.getAuthToken());
        ctx.result(serializer.toJson(res));
    }

    private void logout(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        var res = Map.of("authToken", req.get("authToken"));
        ctx.result(serializer.toJson(res));
    }

    private void list(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        var res = Map.of("authToken", req.get("authToken"));
        ctx.result(serializer.toJson(res));
    }

    private void create(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        var res = Map.of("authToken", req.get("authToken"), "gameName", req.get("gameName"));
        ctx.result(serializer.toJson(res));
    }

    private void join(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        var res = Map.of("authToken", req.get("authToken"), "playerColor", req.get("playerColor"), "gameID", req.get("gameID"));
        ctx.result(serializer.toJson(res));
    }

    private void clear(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        var res = Map.of();
        ctx.result(serializer.toJson(res));
    }

    public void stop() {
        javalinObj.stop();
    }
}
