package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import java.util.Map;
import model.*;
import dataaccess.*;
import service.*;

public class Server {

    //private final UserService userService;
    DataAccess dataAccess = new MemoryDataAccess();
    private final Javalin javalinObj;

    public Server() {
        //userService = new UserService(null);


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
        try {
            var serializer = new Gson();
            var user = serializer.fromJson(ctx.body(), UserData.class);
            var service = new UserService(dataAccess);
            var regResponse = service.register(user);
            ctx.result(serializer.toJson(regResponse));
        }
        catch (Exception ex) {
            exception(ex, ctx);
        }
    }

    private void login(Context ctx) {
        try {
            var serializer = new Gson();
            var user = serializer.fromJson(ctx.body(), UserData.class);
            var service = new UserService(dataAccess);
            var loginResponse = service.login(user);
            ctx.result(serializer.toJson(loginResponse));
        }
        catch (Exception ex) {
            exception(ex, ctx);
        }
    }

    private void logout(Context ctx) {
        try {
            var serializer = new Gson();
            var user = ctx.header("authorization");
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
        try {
            var serializer = new Gson();
            var authToken = ctx.header("authorization");
            var service = new UserService(dataAccess);
            var listResponse = service.list(authToken);
            ctx.result(serializer.toJson(listResponse));
        }
        catch (Exception ex) {
            String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
            ctx.status(401).result(message);
        }
    }

    private void create(Context ctx) {
        try {
            var serializer = new Gson();
            var auth = ctx.header("authorization");
            var game = serializer.fromJson(ctx.body(), GameData.class);
            var service = new UserService(dataAccess);
            var createResponse = service.create(game.gameName(), auth);
            ctx.result(serializer.toJson(createResponse));
        }
        catch (Exception ex) {
            exception(ex, ctx);
        }
    }

    private void join(Context ctx) throws DataAccessException {
        try {
            var serializer = new Gson();
            var auth = ctx.header("authorization");
            var input = serializer.fromJson(ctx.body(), Map.class);
            var joinRequest = new JoinData((Double) input.get("gameID"), (String) input.get("playerColor"), "gameName");
            var service = new UserService(dataAccess);
            var joinResponse = service.join(joinRequest, auth);
            ctx.result(serializer.toJson(joinResponse));
        }
        catch (Exception ex) {
            exception(ex, ctx);
        }
    }

    private void clear(Context ctx) {
        var service = new UserService(dataAccess);
        service.clear();
        ctx.result("{}");
    }

    public void stop() {
        javalinObj.stop();
    }

    public void exception(Exception ex, Context ctx) {
        switch (ex.getMessage()) {
            case "Error: bad request" -> {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(400).result(message);
            }
            case "Error: unauthorized" -> {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(401).result(message);
            }
            case "Error: Forbidden" -> {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(403).result(message);
            }
            default -> {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(500).result(message);
            }
        }
    }
}
