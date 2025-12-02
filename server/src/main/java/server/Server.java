package server;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import io.javalin.websocket.WsMessageContext;
import model.*;
import dataaccess.*;
import service.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

public class Server {

    private final ConnectionManager gameConnections = new ConnectionManager();

    public DataAccess dataAccess;

    private final Javalin javalinObj;

    public Server() {
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

        javalinObj.delete("db", this::clear);

        javalinObj.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
               ctx.enableAutomaticPings();
               System.out.println("Connected");
            });
            ws.onMessage(ctx -> {
                var gson = new Gson();
                UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);

                switch(command.getCommandType()) {
                    case CONNECT -> {
                        gameConnections.addConnection(command.getGameID(), ctx.session);
                    }
                    case MAKE_MOVE -> {
                        move(command, ctx);
                    }
                    case LEAVE -> {
                        gameConnections.removeConnection(ctx.session);
                    }
                    case RESIGN -> {
                        gameConnections.removeConnection(ctx.session);
                    }
                }
            });
            ws.onClose(_ -> System.out.println("Disconnected"));
        });
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

    private void move(UserGameCommand command, WsMessageContext ctx) throws IOException {
        try {
            var serializer = new Gson();
            var auth = command.getAuthToken();
            var targetID = Double.valueOf(command.getGameID());
            var move = command.getMove();

            var moveRequest = new MoveData(targetID, move);
            var service = new UserService(dataAccess);
            var moveResponse = service.move(moveRequest, auth);
            var loadMessage = new LoadGameMessage(new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME), moveResponse.board());
            var gson = new Gson();
            var jsonMessage = gson.toJson(loadMessage);
            for (var session : gameConnections.getAllSessions(targetID)) {
                session.getRemote().sendString((gson.toJson(jsonMessage)));
            }

        } catch (Exception ex) {
            var errorMessage = "Error: " + ex.getMessage();
            var gson = new Gson();
            ctx.session.getRemote().sendString(gson.toJson(errorMessage));
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
