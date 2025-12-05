package server;

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
import webmodel.*;
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
        } catch (DataAccessException ex) {
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
                webSocketHelper(command, ctx);

            });
            ws.onClose(ctx -> System.out.println("Disconnected"));
        });
    }


    public void webSocketHelper(UserGameCommand command, WsMessageContext ctx) throws IOException {
        var gson = new Gson();
        switch (command.getCommandType()) {
            case CONNECT -> {
                try {
                    gameConnections.addConnection(command.getGameID(), ctx.session);
                    load(command, ctx);
                    connect(command, ctx);
                } catch (Exception ex) {
                    gameConnections.removeConnection(ctx.session);
                    var errorMessage = ex.getMessage();
                    var output = new NotifGameResponse(ServerMessage.ServerMessageType.ERROR, null, errorMessage);
                    try {
                        if (ctx.session.isOpen()) {
                            ctx.session.getRemote().sendString(gson.toJson(output));
                        }
                    } catch (Exception asdf) {

                    }
                }
            }
            case MAKE_MOVE -> {
                try {
                    move(command, ctx);
                } catch (Exception ex) {
                    var errorMessage = ex.getMessage();
                    var output = new NotifGameResponse(ServerMessage.ServerMessageType.ERROR, null, errorMessage);
                    try {
                        if (ctx.session.isOpen()) {
                            ctx.session.getRemote().sendString(gson.toJson(output));
                        }
                    } catch (Exception asf) {

                    }
                }
            }
            case LEAVE -> {
                leave(command, ctx);
                gameConnections.removeConnection(ctx.session);
            }
            case RESIGN -> {
                resign(command, ctx);
            }
            case LOAD -> {
                try {
                    load(command, ctx);
                } catch (Exception ex) {
                    var errorMessage = ex.getMessage();
                    var output = new NotifGameResponse(ServerMessage.ServerMessageType.ERROR, null, errorMessage);
                    try {
                        if (ctx.session.isOpen()) {
                            ctx.session.getRemote().sendString(gson.toJson(output));
                        }
                    } catch (Exception a) {

                    }
                }
            }
            case HIGHLIGHT -> {
                highlight(command, ctx);
            }
        }
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
        } catch (Exception ex) {
            baseErrorHelper(ctx, ex);
        }
    }

    private void login(Context ctx) {
        try {
            var serializer = new Gson();
            var user = serializer.fromJson(ctx.body(), UserData.class);
            var service = new UserService(dataAccess);
            var loginResponse = service.login(user);
            ctx.result(serializer.toJson(loginResponse));
        } catch (Exception ex) {
            baseErrorHelper(ctx, ex);
        }
    }

    private void logout(Context ctx) {
        try {
            var serializer = new Gson();
            var user = ctx.header("authorization");
            var service = new UserService(dataAccess);
            var logoutResponse = service.logout(user);
            ctx.result(serializer.toJson(logoutResponse));
        } catch (Exception ex) {
            baseErrorHelper(ctx, ex);
        }
    }

    private void list(Context ctx) {
        try {
            var serializer = new Gson();
            var authToken = ctx.header("authorization");
            var service = new UserService(dataAccess);
            var listResponse = service.list(authToken);
            ctx.result(serializer.toJson(listResponse));
        } catch (Exception ex) {
            baseErrorHelper(ctx, ex);
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
        } catch (Exception ex) {
            baseErrorHelper(ctx, ex);
        }
    }

    private void baseErrorHelper(Context ctx, Exception ex) {
        switch (ex.getMessage()) {
            case "Error: bad request" -> {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(400).result(message);
            }
            case "Error: unauthorized" -> {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(401).result(message);
            }
            case "Error: User already exists", "Error: Forbidden" -> {
                String message = String.format("{\"message\": \"%s\"}", ex.getMessage());
                ctx.status(403).result(message);
            }
            default -> {
                String message = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
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
        } catch (Exception ex) {
            baseErrorHelper(ctx, ex);
        }
    }

    private void move(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
        var auth = command.getAuthToken();
        var targetID = Double.valueOf(command.getGameID());
        var move = command.getMove();
        var moveRequest = new MoveData(targetID, move);
        var service = new UserService(dataAccess);
        var moveResponse = service.move(moveRequest, auth);
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, moveResponse.board());
        var gson = new Gson();
        var jsonMessage = gson.toJson(loadMessage);
        var moveMade = new NotifGameResponse(ServerMessage.ServerMessageType.NOTIFICATION,
                (moveResponse.user() + " has made a move from" + command.getLocation()), null);
        var jsonNotif = gson.toJson(moveMade);
        for (var session : gameConnections.getAllSessions(targetID)) {
            try {
                if (ctx.session.isOpen()) {
                    session.getRemote().sendString(jsonMessage);
                }
                if (session == ctx.session) {
                    continue;
                }
                if (ctx.session.isOpen()) {
                    session.getRemote().sendString(jsonNotif);
                }
            } catch (Exception a) {

            }
        }
        if (!Objects.equals(moveResponse.gameState(), "")) {
            var gameUpdate = new NotifGameResponse(ServerMessage.ServerMessageType.NOTIFICATION,
                    (moveResponse.gameState()), null);
            var jsonGameState = gson.toJson(gameUpdate);
            for (var session : gameConnections.getAllSessions(targetID)) {
                try {
                    if (ctx.session.isOpen()) {
                        session.getRemote().sendString(jsonGameState);
                    }
                } catch (Exception a) {

                }
            }
        }
    }

    private void load(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
        var auth = command.getAuthToken();
        var targetID = Double.valueOf(command.getGameID());
        var loadRequest = new LoadGameData(targetID, auth);
        var service = new UserService(dataAccess);
        var loadResponse = service.load(loadRequest);
        var loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, loadResponse.board());
        var gson = new Gson();
        var message = gson.toJson(loadMessage);
        try {
            if (ctx.session.isOpen()) {
                ctx.session.getRemote().sendString(message);
            }
        } catch (Exception a) {

        }
    }

    private void leave(UserGameCommand command, WsMessageContext ctx) throws IOException {
        try {
            var auth = command.getAuthToken();
            var targetID = Double.valueOf(command.getGameID());
            var leaveRequest = new LeaveGameData(targetID, auth);
            var service = new UserService(dataAccess);
            var leaveResponse = service.leave(leaveRequest);
            var leaveString = leaveResponse.message();
            var leaveMessage = new NotifGameResponse(ServerMessage.ServerMessageType.NOTIFICATION, leaveString, null);
            var gson = new Gson();
            var message = gson.toJson(leaveMessage);
            for (var session : gameConnections.getAllSessions(targetID)) {
                try {
                    if (session == ctx.session) {
                        continue;
                    }
                    if (ctx.session.isOpen()) {
                        session.getRemote().sendString(message);
                    }
                } catch (Exception a) {

                }
            }
        } catch (Exception ex) {
            errorHelper(ex, ctx);
        }
    }

    private void highlight(UserGameCommand command, WsMessageContext ctx) throws IOException {
        try {
            var auth = command.getAuthToken();
            var targetID = Double.valueOf(command.getGameID());
            var highRequest = new HighGameData(targetID, auth, command.getLocation());
            var service = new UserService(dataAccess);
            var highResponse = service.highlight(highRequest);
            var highMessage = new HighlightMessage(ServerMessage.ServerMessageType.LOAD_HIGHLIGHT,
                    highResponse.allPieces(),
                    highResponse.moves());
            var gson = new Gson();
            try {
                if (ctx.session.isOpen()) {
                    ctx.session.getRemote().sendString(gson.toJson(highMessage));
                }
            } catch (Exception a) {

            }
        } catch (Exception ex) {
            errorHelper(ex, ctx);
        }
    }

    private void connect(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
            String username = command.getAuthToken();
            var service = new UserService(dataAccess);
            var connectReturn = service.connect(username);
            String playerColor = command.getLocation();
            String connectMessage = connectReturn.username() + " has connected to the game as " + playerColor;
            var gson = new Gson();
            var message = new NotifGameResponse(ServerMessage.ServerMessageType.NOTIFICATION, connectMessage, null);
            for (var session : gameConnections.getAllSessions(command.getGameID())) {
                try {
                    if (session == ctx.session) {
                        continue;
                    }
                    if (ctx.session.isOpen()) {
                        session.getRemote().sendString(gson.toJson(message));
                    }
                } catch (Exception a) {

                }
            }
    }

    private void resign(UserGameCommand command, WsMessageContext ctx) throws IOException {
        try {
            String auth = command.getAuthToken();
            String username = command.getLocation();
            String resignMessage = username + " has resigned";
            var message = new NotifGameResponse(ServerMessage.ServerMessageType.NOTIFICATION, resignMessage, null);
            var service = new UserService(dataAccess);
            service.resign(auth, command.getGameID().doubleValue());
            var gson = new Gson();
            for (var session : gameConnections.getAllSessions(command.getGameID())) {
                try {
                    if (ctx.session.isOpen()) {
                        session.getRemote().sendString(gson.toJson(message));
                    }
                } catch (Exception a) {

                }
            }
        } catch (Exception ex) {
            errorHelper(ex, ctx);
        }
    }

    private void errorHelper(Exception ex, WsMessageContext ctx) {
        var errorMessage = ex.getMessage();
        var output = new NotifGameResponse(ServerMessage.ServerMessageType.ERROR, null, errorMessage);
        var gson = new Gson();
        try {
            if (ctx.session.isOpen()) {
                ctx.session.getRemote().sendString(gson.toJson(output));
            }
        } catch (Exception a) {

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
