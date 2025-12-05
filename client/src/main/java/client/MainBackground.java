package client;

import chess.*;
import client.ui.MainHelper;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.ListResponse;
import websocket.commands.UserGameCommand;
import static client.ui.EscapeSequences.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.*;

public class MainBackground {

    public String user = null;
    private String userAuth = "";
    private WebSocket webSocket;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String serverUrl;
    boolean loggedIn = false;
    private String playerColor = null;
    private String highlightPosition;
    private final MainHelper help = new MainHelper();

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

    public void gameAction(String result, String requestInput, String gameID, String playerColor, Scanner scanner) {
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
                        whitePlayer = (game.whiteUsername() == null) ? "Open" : game.whiteUsername();
                        blackPlayer = (game.blackUsername() == null) ? "Open" : game.blackUsername();
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
                    if (Objects.equals(playerColor, "BLUE")) {
                        this.playerColor = playerColor;
                        boolean observer = true;
                        gameMode(gameID, observer, scanner);
                        return;
                    }
                    var request = HttpRequest.newBuilder()
                            .uri(new URI(registerUrl))
                            .header("Authorization", userAuth)
                            .timeout(java.time.Duration.ofMillis(5000))
                            .PUT(HttpRequest.BodyPublishers.ofString(requestInput))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        this.playerColor = playerColor;
                        boolean observer = false;
                        gameMode(gameID, observer, scanner);
                    } else {
                        help.joinErrors(response);
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

    public void clear(Scanner scanner) {
        var gson = new Gson();
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

    public void boardPrinterHighlight(Map<String, ChessPiece> board, boolean highlight, Collection<ChessMove> validMoves) {
        var bG = SET_BG_COLOR_LIGHT_GREY;
        var bWV = SET_BG_COLOR_WHITE; //This variable stands for background White Variable
        var bBV = SET_BG_COLOR_BLACK;
        Collection<ChessPosition> positions = new HashSet<>();
        if (highlight) {
            for (ChessMove move : validMoves) {
                positions.add(move.getEndPosition());
            }
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
            letters = SET_BG_COLOR_LIGHT_GREY + "     h   g  f   e   d   c  b   a   " + SET_BG_COLOR_BLACK;
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
                    System.out.printf(bWV + help.pieceName(place, board));
                    bWV = SET_BG_COLOR_WHITE;
                } else {
                    if (positions.contains(place)) {
                        bBV = SET_BG_COLOR_RED;
                    }
                    System.out.printf(bBV + help.pieceName(place, board));
                    bBV = SET_BG_COLOR_BLACK;
                }
            }
            System.out.printf(bG + " " + SET_TEXT_COLOR_BLACK + i + " " + SET_BG_COLOR_BLACK + "\n");
        }
        System.out.println(SET_TEXT_COLOR_BLACK + letters + "\n");
    }

    private boolean tileColor(int i, int j) {
        boolean iCheck = (i % 2 == 0);
        boolean jCheck = (j % 2 == 0);
        return iCheck == jCheck;
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

    public void gameMode(String gameID, boolean observer, Scanner scanner) {
        var gson = new Gson();
        String cR = SET_TEXT_COLOR_YELLOW + SET_BG_COLOR_DARK_GREEN; //cR stands for command Request
        try {
            String wsUrl = serverUrl.replace("http", "ws") + "/ws";

            webSocket = HttpClient.newHttpClient().newWebSocketBuilder()
                    .header("Authorization", userAuth)
                    .buildAsync(URI.create(wsUrl), new MyWebSocketListener(this))
                    .join();

            var commandType = UserGameCommand.CommandType.CONNECT;
            String playerColor;
            if (observer) {
                playerColor = "an observer";
            } else {
                if (Objects.equals(this.playerColor, "WHITE")) {
                    playerColor = "white";
                } else {
                    playerColor = "black";
                }
            }
            var command = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID), playerColor);
            var startGame = gson.toJson(command);
            webSocket.sendText(startGame, true);
        } catch (Exception ex) {
            System.out.println("Failed to connect to websocket");
            return;
        }
        if (observer) {
            help.observer(cR, gameID, scanner, webSocket, userAuth, gson, highlightPosition, this);
            return;
        }
        boolean stillGoing = true;
        String result = "";
        while (stillGoing) {
            if (!Objects.equals(result, "")) {
                System.out.println(cR + "[GameMode] What is your command?" + SET_BG_COLOR_BLACK);
            }
            try {
                result = scanner.nextLine().trim();
            } catch (Exception ex) {
                System.out.println("failedToGetInput");
                continue;
            }
            String[] commands = result.split("\\s+");
            UserGameCommand.CommandType commandType = UserGameCommand.CommandType.LOAD;
            UserGameCommand command;
            switch (commands[0]) {
                case ("resign") -> {
                    System.out.println(cR + "Are you sure that you want to resign? (Y or N)" + SET_BG_COLOR_BLACK);
                    var answer = scanner.next();
                    if (!Objects.equals(answer, "Y")) {
                        continue;
                    }
                    commandType = UserGameCommand.CommandType.RESIGN;
                    command = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID), user);
                    webSocket.sendText(gson.toJson(command), true);
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
                    String position = String.valueOf((firstNumber*10) + (highlightPosition.charAt(1) - '0'));
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
                    help.printer(commands);
                }
                case ("move") -> {
                    if (commands.length < 3) {
                        System.out.println("Please retry your move command");
                        continue;
                    }
                    help.move(commands, this, gameID, userAuth, webSocket);
                }
                case ("") -> {
                    continue;
                }
                default -> {
                    System.out.println("Not a valid command, please use help to see valid commands");
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