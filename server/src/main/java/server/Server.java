package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import java.util.Map;
import model.*;
import dataaccess.*;
import service.*;

public class Server {

    private final UserService userService;
    DataAccess dataAccess = new MemoryDataAccess();
    private final Javalin javalinObj;

    public Server() {
        userService = new UserService(null);


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

    private void register(Context ctx) throws DataAccessException {
        try {
            var serializer = new Gson();
            var user = serializer.fromJson(ctx.body(), UserData.class);
            var service = new UserService(dataAccess);
            var regResponse = service.register(user);
            ctx.result(serializer.toJson(regResponse));
        }
        catch (Exception ex) {
            String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
            ctx.status(403).result(message);
        }
    }

    private void login(Context ctx) throws DataAccessException {
        try {
            var serializer = new Gson();
            var user = serializer.fromJson(ctx.body(), UserData.class);
            var service = new UserService(dataAccess);
            var loginResponse = service.login(user);
            ctx.result(serializer.toJson(loginResponse));
        }
        catch (Exception ex) {
            String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
            ctx.status(401).result(message);
        }
    }

    private void logout(Context ctx) {
        try {
            var serializer = new Gson();
            var user = serializer.fromJson(ctx.body(), AuthData.class);
            var service = new UserService(dataAccess);
            var logoutResponse = service.logout(user);
            ctx.result(serializer.toJson(logoutResponse));
        }
        catch (Exception ex) {
            String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
            ctx.status(401).result(message);
        }
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

        var res = ctx.result("{}");
    }

    public void stop() {
        javalinObj.stop();
    }
}
