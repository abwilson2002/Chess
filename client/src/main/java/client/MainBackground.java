package client;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import model.AuthData;
import model.GameData;
import model.ListResponse;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import static client.ui.EscapeSequences.*;
import static chess.ChessPiece.PieceType.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletionStage;

public class MainBackground {

    public String user = null;
    private String userAuth = "";
    private WebSocket webSocket;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String serverUrl;
    boolean loggedIn = false;
    private String playerColor = null;
    private String highlightPosition;

    public MainBackground(String serverName) throws Exception {

        serverUrl = serverName;

    }


    public void addUser(String pathedUrl, String requestInput) {
        var gson = new Gson();
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(pathedUrl))
                    .header("Authorization", userAuth)
                    .timeout(java.time.Duration.ofMillis(5000))
                    .POST(HttpRequest.BodyPublishers.ofString(requestInput))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                switch (response.statusCode()) {
                    case (400) -> {
                        System.out.println("The command was incorrectly entered. Check for spelling and syntax, use help if needed" +
                                SET_BG_COLOR_BLACK + "\n");
                    }
                    case (401) -> {
                        System.out.println("You are not authorized to login with those credentials" +
                                SET_BG_COLOR_BLACK + "\n");
                    }
                    case (403) -> {
                        System.out.println("Someone has already taken that username" +
                                SET_BG_COLOR_BLACK + "\n");
                    }
                    case (500) -> {
                        System.out.println("Make sure the server has started" +
                                SET_BG_COLOR_BLACK + "\n");
                    }
                }
            }
            String responseBody = response.body();

            AuthData output = gson.fromJson(responseBody, AuthData.class);

            userAuth = output.authToken();
            user = output.username();

            System.out.println("Logged in as: " + user +
                    SET_BG_COLOR_BLACK + "\n");
            loggedIn = true;
        } catch (Exception ex) {
            System.out.println("Not a valid input, use help to see command structure" +
                    SET_BG_COLOR_BLACK + "\n");
        }
    }

    public void logoutUser() {
        if (!loggedIn) {
            System.out.println("You are not logged in yet" +
                    SET_BG_COLOR_BLACK + "\n");
            return;
        }
        try {
            String registerUrl = serverUrl + "/session";

            var request = HttpRequest.newBuilder()
                    .uri(new URI(registerUrl))
                    .header("Authorization", userAuth)
                    .timeout(java.time.Duration.ofMillis(5000))
                    .DELETE()
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Logged out" +
                    SET_BG_COLOR_BLACK + "\n");

            loggedIn = false;
        } catch (Exception ex) {
            System.out.println("Error: Could not logout");
        }
    }

    public void gameAction(String result, String requestInput, String gameID) {
        var gson = new Gson();
        if (!loggedIn) {
            System.out.println("Please log in or register before continuing" +
                    SET_BG_COLOR_BLACK + "\n");
            return;
        }
        try {
            String registerUrl = serverUrl + "/game";
            switch (result) {
                case ("list") -> {
                    var request = HttpRequest.newBuilder()
                            .uri(new URI(registerUrl))
                            .header("Authorization", userAuth)
                            .timeout(java.time.Duration.ofMillis(5000))
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String responseBody = response.body();

                    ListResponse rawOutput = gson.fromJson(responseBody, ListResponse.class);
                    List<GameData> output = rawOutput.games();

                    int i = 1;
                    String whitePlayer = "Open";
                    String blackPlayer = "Open";
                    for (GameData game : output) {
                        if (game.whiteUsername() != null) {
                            whitePlayer = game.whiteUsername();
                        }
                        if (game.blackUsername() != null) {
                            blackPlayer = game.blackUsername();
                        }
                        System.out.printf(SET_BG_COLOR_BLUE +
                                        SET_TEXT_COLOR_RED +
                                        "Game: %d whiteUser: %s blackUser: %s GameName: %s" +
                                        SET_BG_COLOR_BLACK + "\n",
                                i,
                                whitePlayer,
                                blackPlayer,
                                game.gameName()
                        );
                        i++;
                    }
                }
                case ("create") -> {
                    var request = HttpRequest.newBuilder()
                            .uri(new URI(registerUrl))
                            .header("Authorization", userAuth)
                            .timeout(java.time.Duration.ofMillis(5000))
                            .POST(HttpRequest.BodyPublishers.ofString(requestInput))
                            .build();

                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println("Game created" +
                            SET_BG_COLOR_BLACK + "\n");
                }
                case ("join") -> {
                    var request = HttpRequest.newBuilder()
                            .uri(new URI(registerUrl))
                            .header("Authorization", userAuth)
                            .timeout(java.time.Duration.ofMillis(5000))
                            .PUT(HttpRequest.BodyPublishers.ofString(requestInput))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        this.playerColor = playerColor;
                        boolean observer = (Objects.equals(playerColor, "BLUE"));
                        gameMode(gameID, observer);
                    } else {
                        switch (response.statusCode()) {
                            case (400) -> {
                                System.out.println("The command was incorrectly entered. Check for spelling and syntax, use help if needed" +
                                        SET_BG_COLOR_BLACK + "\n");
                            }
                            case (401) -> {
                                System.out.println("Login required before games can be joined" +
                                        SET_BG_COLOR_BLACK + "\n");
                            }
                            case (403) -> {
                                System.out.println("Someone has already taken that spot" +
                                        SET_BG_COLOR_BLACK + "\n");
                            }
                            case (500) -> {
                                System.out.println("Make sure the server has started" +
                                        SET_BG_COLOR_BLACK + "\n");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error: Could not interact with game at this time");
        }

    }

    public void help() {
        if (!loggedIn) {
            System.out.println("register <username> <password> <email> : this registers a new user with the given credentials" +
                    SET_BG_COLOR_BLACK);
            System.out.println("login <username> <password> : this logs you in as a preexisting user" +
                    SET_BG_COLOR_BLACK);
            System.out.println("exit : this ends the chess client + \n" +
                    SET_BG_COLOR_BLACK + "\n");
        } else {
            System.out.println("logout : this logs you out" +
                    SET_BG_COLOR_BLACK);
            System.out.println("list : this will list all of the games currently saved" +
                    SET_BG_COLOR_BLACK);
            System.out.println("create <gameName> : this will create a new game with the given name" +
                    SET_BG_COLOR_BLACK);
            System.out.println("join <gameID> <color> : this will join you as a player or spectator " +
                    "depending on the your input (use all caps, spectators use BLUE)" +
                    SET_BG_COLOR_BLACK + "\n");
        }
    }

    public void clear() {
        var gson = new Gson();
        var scanner = new Scanner(System.in);
        System.out.println("You have selected clear, enter your manager password to clear" +
                SET_BG_COLOR_BLACK);

        var result = scanner.next();
        if (!Objects.equals(result,
                "youmustacceptthatbydoingthisyouarejepreodizingthesaveinformationofall2peoplethatwilleventuallyusethis..." +
                        "thinkcarefu11ybeforeyoueneter")) {
            System.out.println("password incorrect, you will now be exited from the program" +
                    SET_BG_COLOR_BLACK);

        } else {
            System.out.println("password accepted, clearing now\n");

            String registerUrl = serverUrl + "/db";
            try {
                var request = HttpRequest.newBuilder()
                        .uri(new URI(registerUrl))
                        .header("Authorization", userAuth)
                        .timeout(java.time.Duration.ofMillis(5000))
                        .DELETE()
                        .build();

                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception ex) {
                System.out.println("Error: Could not clear data");
            }
        }
    }

    public void boardPrinter(Map<String, ChessPiece> board) {
        var bG = SET_BG_COLOR_LIGHT_GREY;
        int startLetter = 1;
        int endLetter = 9;
        int startNumber = 8;
        int endNumber = 0;
        int directionLetter = 1;
        int directionNumber = -1;
        boolean white = true;
        String letters = SET_BG_COLOR_LIGHT_GREY + "     a   b  c   d   e  f   g   h   " + SET_BG_COLOR_BLACK;
        if (Objects.equals(playerColor, "BLACK")) {
            startLetter = 8;
            endLetter = 0;
            startNumber = 1;
            endNumber = 9;
            directionLetter = -1;
            directionNumber = 1;
            white = false;
            letters = "  h   g  f   e   d   c  b   a   " + SET_BG_COLOR_BLACK;
        }
        System.out.println(SET_TEXT_COLOR_BLACK + letters);
        for (int i = startNumber; (white ? (i > endNumber) : (i < endNumber)); i += directionNumber) {
            System.out.printf(bG + " " + SET_TEXT_COLOR_BLACK + i + " ");
            for (int j = startLetter; (white ? (j < endLetter) : (j > endLetter)); j += directionLetter) {
                var place = new ChessPosition(i, j);
                if (tileColor(i, j)) {
                    System.out.printf(SET_BG_COLOR_WHITE + pieceName(place, board));
                } else {
                    System.out.printf(SET_BG_COLOR_BLACK + pieceName(place, board));
                }
            }
            System.out.printf(bG + " " + SET_TEXT_COLOR_BLACK + i + " " + SET_BG_COLOR_BLACK + "\n");
        }
        System.out.println(SET_TEXT_COLOR_BLACK + letters);
    }

    public void boardPrinterHighlight(Map<String, ChessPiece> board, Collection<ChessMove> validMoves) {
        var bG = SET_BG_COLOR_LIGHT_GREY;
        var bWV = SET_BG_COLOR_WHITE; //This variable stands for background White Variable
        var bBV = SET_BG_COLOR_BLACK;
        Collection<ChessPosition> positions = new HashSet<>();
        for (ChessMove move : validMoves) {
            positions.add(move.getEndPosition());
        }
        int startLetter = 1;
        int endLetter = 9;
        int startNumber = 8;
        int endNumber = 0;
        int directionLetter = 1;
        int directionNumber = -1;
        boolean white = true;
        String letters = SET_BG_COLOR_LIGHT_GREY + "     a   b  c   d   e  f   g   h   " + SET_BG_COLOR_BLACK;
        if (Objects.equals(playerColor, "BLACK")) {
            startLetter = 8;
            endLetter = 0;
            startNumber = 1;
            endNumber = 9;
            directionLetter = -1;
            directionNumber = 1;
            white = false;
            letters = "  h   g  f   e   d   c  b   a   " + SET_BG_COLOR_BLACK;
        }
        System.out.println(SET_TEXT_COLOR_BLACK + letters);
        for (int i = startNumber; (white ? (i > endNumber) : (i < endNumber)); i += directionNumber) {
            System.out.printf(bG + " " + SET_TEXT_COLOR_BLACK + i + " ");
            for (int j = startLetter; (white ? (j < endLetter) : (j > endLetter)); j += directionLetter) {
                var place = new ChessPosition(i, j);
                if (tileColor(i, j)) {
                    if (positions.contains(place)) {
                        bWV = SET_BG_COLOR_YELLOW;
                    }
                    System.out.printf(bWV + pieceName(place, board));
                    bWV = SET_BG_COLOR_WHITE;
                } else {
                    if (positions.contains(place)) {
                        bBV = SET_BG_COLOR_RED;
                    }
                    System.out.printf(bBV + pieceName(place, board));
                    bBV = SET_BG_COLOR_BLACK;
                }
            }
            System.out.printf(bG + " " + SET_TEXT_COLOR_BLACK + i + " " + SET_BG_COLOR_BLACK + "\n");
        }
        System.out.println(SET_TEXT_COLOR_BLACK + letters);
    }

    private boolean tileColor(int i, int j) {
        boolean iCheck = (i % 2 == 0);
        boolean jCheck = (j % 2 == 0);
        return iCheck == jCheck;
    }

    private String pieceName(ChessPosition place, Map<String, ChessPiece> board) {
        String encodedPiece = EMPTY;
        ChessPiece piece = board.get(ChessBoard.positionToString(place));
        if (piece == null) {
            return encodedPiece;
        }
        ChessPiece.PieceType type = piece.getPieceType();
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            String pieceColor = SET_TEXT_COLOR_ORANGE;
            if (type == PAWN) {
                encodedPiece = pieceColor + WHITE_PAWN;
            } else if (type == ROOK) {
                encodedPiece = pieceColor + WHITE_ROOK;
            } else if (type == KNIGHT) {
                encodedPiece = pieceColor + WHITE_KNIGHT;
            } else if (type == BISHOP) {
                encodedPiece = pieceColor + WHITE_BISHOP;
            } else if (type == QUEEN) {
                encodedPiece = pieceColor + WHITE_QUEEN;
            } else {
                encodedPiece = pieceColor + WHITE_KING;
            }
        } else {
            String pieceColor = SET_TEXT_COLOR_GREEN;
            if (type == PAWN) {
                encodedPiece = pieceColor + BLACK_PAWN;
            } else if (type == ROOK) {
                encodedPiece = pieceColor + BLACK_ROOK;
            } else if (type == KNIGHT) {
                encodedPiece = pieceColor + BLACK_KNIGHT;
            } else if (type == BISHOP) {
                encodedPiece = pieceColor + BLACK_BISHOP;
            } else if (type == QUEEN) {
                encodedPiece = pieceColor + BLACK_QUEEN;
            } else {
                encodedPiece = pieceColor + BLACK_KING;
            }
        }
        return encodedPiece;
    }

    public Integer letterToNumber(Character l) {
        switch (l) {
            case ('a') -> {
                return 1;
            }
            case ('b') -> {
                return 2;
            }
            case ('c') -> {
                return 3;
            }
            case ('d') -> {
                return 4;
            }
            case ('e') -> {
                return 5;
            }
            case ('f') -> {
                return 6;
            }
            case ('g') -> {
                return 7;
            }
            case ('h') -> {
                return 8;
            }
        }
        return 0;
    }

    public void gameMode(String gameID, boolean observer) {
        var scanner = new Scanner(System.in);
        var gson = new Gson();
        String cR = SET_TEXT_COLOR_YELLOW + SET_BG_COLOR_DARK_GREEN; //cR stands for command Request
        try {
            String wsUrl = serverUrl.replace("http", "ws") + "/ws";

            webSocket = HttpClient.newHttpClient().newWebSocketBuilder()
                    .header("Authorization", userAuth)
                    .buildAsync(URI.create(wsUrl), new MyWebSocketListener(this))
                    .join();

            var commandType = UserGameCommand.CommandType.CONNECT;
            var command = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID));
            var startGame = gson.toJson(command);

            webSocket.sendText(startGame, true);
        } catch (Exception ex) {
            System.out.println("Failed to connect to websocket");
            return;
        }

        if (observer) {
            System.out.println(cR + "Observing game " + gameID + SET_BG_COLOR_BLACK);

            return;
        }
        var commandType = UserGameCommand.CommandType.LOAD;
        var command = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID));
        webSocket.sendText(gson.toJson(command), true);
        boolean stillGoing = true;
        while (stillGoing) {
            System.out.println(cR + "[GameMode] What is your command?" + SET_BG_COLOR_BLACK);
            var result = scanner.nextLine().trim();
            String[] commands = result.split("\\s+");
            switch (commands[0]) {
                case ("resign") -> {
                    commandType = UserGameCommand.CommandType.RESIGN;
                    command = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID));
                    webSocket.sendText(gson.toJson(command), true);
                    stillGoing = false;
                }
                case ("leave") -> {
                    commandType = UserGameCommand.CommandType.LEAVE;
                    command = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID));
                    webSocket.sendText(gson.toJson(command), true);
                    stillGoing = false;
                }
                case ("highlight") -> {
                    highlightPosition = commands[1];
                    Integer firstNumber = letterToNumber(highlightPosition.charAt(0));
                    String position = String.valueOf((firstNumber*10) + highlightPosition.charAt(1));
                    commandType = UserGameCommand.CommandType.HIGHLIGHT;
                    command = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID), position);
                    webSocket.sendText(gson.toJson(command), true);
                }
                case ("update") -> {
                    commandType = UserGameCommand.CommandType.LOAD;
                    command = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID));
                    webSocket.sendText(gson.toJson(command), true);
                }
                case ("help") -> {
                    var text = SET_BG_COLOR_DARK_GREEN + SET_TEXT_COLOR_YELLOW;
                    var bG = SET_BG_COLOR_BLACK;
                    if (commands.length == 1) {

                        System.out.println(text + "resign : forfeits the game for you" + bG);
                        System.out.println(text + "leave : the cowards way out, leave without forfeiting" + bG);
                        System.out.println(text + "highlight <position> : highlights the possible moves at a position" + bG);
                        System.out.println(text + "move <move from position> <move to position> <promotion piece> : " +
                                "move a piece, see help(2) for details" + bG);
                        System.out.println(text + "help page2 : shows more details about how to input commands" + bG);
                    } else {
                        System.out.println(text + "<position> : type in the position using the number then the letter (ie. 2f or 6a" + bG);
                        System.out.println(text + "promotion : If you can promote, type in the piece's promotion in all caps (ie. QUEEN)" + bG);
                        System.out.println(text + "            note: leave blank if you cannot promote" + bG);
                    }
                }
                case ("move") -> {
                    var moveStart = commands[1];
                    var moveEnd = commands[2];
                    String promo = "null";
                    ChessPiece.PieceType promote = null;
                    try {
                        if (commands.length > 3) {
                            promo = commands[3];
                        }
                    } catch (Exception ex) {
                        System.out.println("Promotion piece not a real piece");
                        continue;
                    }

                    ChessPosition start = new ChessPosition(
                            ((moveStart.charAt(1) - '0')),
                            letterToNumber((moveStart.charAt(0))));

                    ChessPosition end = new ChessPosition(
                            ((moveEnd.charAt(1) - '0')),
                            letterToNumber((moveEnd.charAt(0))));

                    if (!promo.equals("null")) {
                        promote = ChessPiece.PieceType.valueOf(promo);
                    }
                    var move = new ChessMove(start, end, promote);

                    Integer targetID = Integer.parseInt(gameID);

                    UserGameCommand moveCommand = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, userAuth, targetID, move);

                    var moveInput = gson.toJson(moveCommand);

                    webSocket.sendText(moveInput, true);

                    /*String registerUrl = serverUrl + "/game/play";
                    try {
                        var request = HttpRequest.newBuilder()
                                .uri(new URI(registerUrl))
                                .header("Authorization", userAuth)
                                .timeout(java.time.Duration.ofMillis(5000))
                                .PUT(HttpRequest.BodyPublishers.ofString(mapInput))
                                .build();

                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() == 200) {
                            var responseBody = response.body();

                            JsonElement root = JsonParser.parseString(responseBody);

                            JsonObject allPiecesMap = root.getAsJsonObject().getAsJsonObject("board");

                            Type type = new TypeToken<Map<String, ChessPiece>>() {
                            }.getType();

                            Map<String, ChessPiece> progress = gson.fromJson(allPiecesMap, type);

                            boardPrinter(progress);
                        } else if (response.statusCode() == 202) {
                            System.out.println(cR + response.body() + SET_BG_COLOR_BLACK);
                        } else {
                            throw new Exception(response.body());
                        }

                    } catch (Exception ex) {
                        System.out.println("Error: Could not complete command");
                    }*/
                }
            }
        }
        try {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing").join();
        } catch (Exception ex) {
            return;
        }
    }
}

class MyWebSocketListener implements WebSocket.Listener {

    private final MainBackground thisInstance;

    public MyWebSocketListener(MainBackground instance) {
        thisInstance = instance;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
    }

    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        var gson = new Gson();

        JsonElement root = JsonParser.parseString(message);

        JsonObject commandType = root.getAsJsonObject().getAsJsonObject("type");

        ServerMessage command = gson.fromJson(commandType, ServerMessage.class);

        switch (command.getServerMessageType()) {
            case LOAD_GAME -> {
                Type type = new TypeToken<Map<String, ChessPiece>>() {
                }.getType();

                JsonObject allPiecesMap = root.getAsJsonObject().getAsJsonObject("board");

                Map<String, ChessPiece> progress = gson.fromJson(allPiecesMap, type);

                this.thisInstance.boardPrinter(progress);
            }
            case ERROR -> {
                JsonObject errorMessage = root.getAsJsonObject().getAsJsonObject("message");

                String result = gson.fromJson(errorMessage, String.class);

                System.out.println(result);
            }
            case NOTIFICATION -> {
                String notif = gson.fromJson(message, String.class);
                System.out.println(notif);
            }
            case LOAD_HIGHLIGHT -> {
                Type type = new TypeToken<Map<String, ChessPiece>>() {
                }.getType();

                JsonObject board = root.getAsJsonObject().getAsJsonObject("board");

                Map<String, ChessPiece> progress = gson.fromJson(board, type);

                type = new TypeToken<Collection<ChessMove>>() {
                }.getType();

                JsonObject vMoves = root.getAsJsonObject().getAsJsonObject("moves");

                Collection<ChessMove> moves = gson.fromJson(vMoves, type);

                this.thisInstance.boardPrinterHighlight(progress, moves);
            }
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    public void onClose(WebSocket webSocket) {
        WebSocket.Listener.super.onClose(webSocket, 200, "Done");
    }

    public void onError(WebSocket webSocket, Exception ex) {
        WebSocket.Listener.super.onError(webSocket, ex);
    }
}
