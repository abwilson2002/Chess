package server;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import java.util.Map;
import java.util.Objects;

import model.*;
import dataaccess.*;
import service.*;

public class Server {

    //private final UserService userService;


    public DataAccess dataAccess;

    private final Javalin javalinObj;

    public Server() {
        //userService = new UserService(null);
        dataAccess = new SqlDataAccess();
        try {
            DatabaseManager.createDatabase();
            dataAccess.init();
        }
        catch (DataAccessException ex) {
            System.err.println("Failed to create tables");
        }


        javalinObj = Javalin.create(config -> config.staticFiles.add("web"));

        javalinObj.post("user", this::register);

        javalinObj.post("session", this::login);

        javalinObj.delete("session", this::logout);

        javalinObj.get("game", this::list);

        javalinObj.post("game", this::create);

        javalinObj.put("game", this::join);

        javalinObj.put("game/play", this::move);

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
            if (ex.getMessage().equals("Error: bad request")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(400).result(message);
            } else if (ex.getMessage().equals("Error: User already exists")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(403).result(message);
            } else {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(500).result(message);
            }
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
            if (ex.getMessage().equals("Error: bad request")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(400).result(message);
            } else if (ex.getMessage().equals("Error: unauthorized")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(401).result(message);
            } else {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(500).result(message);
            }
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
            if (ex.getMessage().equals("Error: unauthorized")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(401).result(message);
            } else {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(500).result(message);
            }
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
            if (ex.getMessage().equals("Error: unauthorized")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(401).result(message);
            } else {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(500).result(message);
            }
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
            if (ex.getMessage().equals("Error: bad request")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(400).result(message);
            } else if (ex.getMessage().equals("Error: unauthorized")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(401).result(message);
            } else {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(500).result(message);
            }
        }
    }

    private void join(Context ctx) throws DataAccessException {
        try {
            var serializer = new Gson();
            var auth = ctx.header("authorization");
            var input = serializer.fromJson(ctx.body(), Map.class);
            if (input.get("gameID") == null) {
                throw new DataAccessException("Error: bad request");
            }
            var idClass = input.get("gameID").getClass();
            double targetID;
            if (idClass == String.class) {
                targetID = Double.parseDouble((String) input.get("gameID"));
            } else {
                targetID = (Double) input.get("gameID");
            }
            var joinRequest = new JoinData(targetID, (String) input.get("playerColor"), "gameName");
            var service = new UserService(dataAccess);
            var joinResponse = service.join(joinRequest, auth);
            ctx.result(serializer.toJson(joinResponse));
        }
        catch (Exception ex) {
            if (ex.getMessage().equals("Error: bad request")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(400).result(message);
            } else if (ex.getMessage().equals("Error: unauthorized")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(401).result(message);
            } else if (ex.getMessage().equals("Error: Forbidden")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(403).result(message);
            } else {
                String message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(500).result(message);
            }
        }
    }

    private void move(Context ctx) throws DataAccessException {
        try {
            var serializer = new Gson();
            var auth = ctx.header("authorization");
            var input = serializer.fromJson(ctx.body(), Map.class);

            ChessPosition start = new ChessPosition(
                    (((String) input.get("start")).charAt(0) - '0'),
                    (((String) input.get("start")).charAt(1) - '0'));

            ChessPosition end = new ChessPosition(
                    (((String) input.get("end")).charAt(0) - '0'),
                    (((String) input.get("end")).charAt(1) - '0'));

            ChessPiece.PieceType promote = null;

            if (!Objects.equals((String) input.get("promote"), "null")) {
                promote = ChessPiece.PieceType.valueOf((String)input.get("promote"));
            }

            var move = new ChessMove(start, end, promote);

            if (input.get("gameID") == null) {
                throw new DataAccessException("Error: bad request");
            }
            double targetID = Double.parseDouble((String) input.get("gameID"));
            var moveRequest = new MoveData(targetID, move);
            var service = new UserService(dataAccess);
            var moveResponse = service.move(moveRequest, auth);
            ctx.result(serializer.toJson(moveResponse));
        } catch (Exception ex) {
            if (ex.getMessage().equals("Error: bad request")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(400).result(message);
            } else if (ex.getMessage().equals("Invalid Move")) {
                String message = "Your move was not valid";
                ctx.status(202).result(message);
            } else if (ex.getMessage().equals("Error: unauthorized")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(401).result(message);
            } else if (ex.getMessage().equals("Error: Forbidden")) {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(403).result(message);
            } else {
                String message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(500).result(message);
            }
        }
    }

    private void clear(Context ctx) {
        var service = new UserService(dataAccess);
        try {
            service.clear();
            dataAccess.init();
            ctx.result("{}");
        } catch (Exception ex){
            String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
            ctx.status(500).result(message);
        }
    }

    public void stop() {
        javalinObj.stop();
    }
}
