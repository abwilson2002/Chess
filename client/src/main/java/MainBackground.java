
import chess.*;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.ListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static ui.EscapeSequences.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class MainBackground {

    private String user = null;
    boolean signedIn = false;
    private String userAuth = "";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String serverUrl;
    boolean loggedIn = false;

    public MainBackground(String serverName) throws Exception {

        serverUrl = serverName;

    }

    public void running() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("♕ 240 Chess Client\n");
        Gson gson = new Gson();

        boolean finished = false;
        while (!finished) {
            System.out.printf(SET_BG_COLOR_DARK_GREEN + SET_TEXT_COLOR_YELLOW + "What is your command?" +
                    SET_BG_COLOR_BLACK + "\n");
            var scanner = new Scanner(System.in);
            var result = scanner.next();

            switch(result) {
                case ("exit") -> {
                    finished = true;
                }
                case ("register"), ("login") -> {
                    String pathedUrl;
                    String requestInput;

                    if (result.equals("register")) {
                        pathedUrl = serverUrl + "/user";
                        String username = scanner.next();
                        String pass = scanner.next();
                        String email = scanner.next();

                        var input = Map.of("username", username, "password", pass, "email", email);
                        requestInput = gson.toJson(input);
                    } else {
                        pathedUrl = serverUrl + "/session";
                        String username = scanner.next();
                        String pass = scanner.next();

                        var input = Map.of("username", username, "password", pass);
                        requestInput = gson.toJson(input);
                    }

                    try {
                        var request = HttpRequest.newBuilder()
                                .uri(new URI(pathedUrl))
                                .header("Authorization", userAuth)
                                .timeout(java.time.Duration.ofMillis(5000))
                                .POST(HttpRequest.BodyPublishers.ofString(requestInput))
                                .build();

                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() != 200) {
                            throw new Exception();
                        }
                        String responseBody = response.body();

                        AuthData output = gson.fromJson(responseBody, AuthData.class);

                        userAuth = output.authToken();
                        user = output.username();

                        System.out.println("Logged in as: " + user +
                                SET_BG_COLOR_BLACK + "\n");
                        loggedIn = true;
                    }
                    catch (Exception ex) {
                        System.out.println("Not a valid input, use help to see command structure" +
                                SET_BG_COLOR_BLACK + "\n");
                    }
                }
                case ("logout") -> {
                    if (!loggedIn) {
                        System.out.println("You are not logged in yet" +
                                SET_BG_COLOR_BLACK + "\n");
                        break;
                    }
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
                }
                case ("list"), ("create"), ("join") -> {
                    if (!loggedIn) {
                        System.out.println("Please log in or register before continuing" +
                                SET_BG_COLOR_BLACK + "\n");
                        break;
                    }
                    String registerUrl = serverUrl + "/game";
                    switch(result) {
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
                            for (GameData game : output) {
                                var bG = SET_BG_COLOR_BLUE;
                                var wC = SET_TEXT_COLOR_RED;
                                System.out.printf(bG + wC + "Game: %d whiteUser: %s blackUser: %s GameName: %s" +
                                                SET_BG_COLOR_BLACK + "\n",
                                        i,
                                        game.whiteUsername(),
                                        game.blackUsername(),
                                        game.gameName()
                                );
                                i++;
                            }
                        }
                        case ("create") -> {
                            String gameName = scanner.next();

                            var input = Map.of("username", user, "gameName", gameName);
                            String requestInput = gson.toJson(input);

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
                            String gameID = scanner.next();
                            String playerColor = scanner.next();

                            var input = Map.of("username", user, "gameID", gameID, "playerColor", playerColor);
                            String requestInput = gson.toJson(input);

                            var request = HttpRequest.newBuilder()
                                    .uri(new URI(registerUrl))
                                    .header("Authorization", userAuth)
                                    .timeout(java.time.Duration.ofMillis(5000))
                                    .PUT(HttpRequest.BodyPublishers.ofString(requestInput))
                                    .build();

                            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                            if (response.statusCode() == 200) {
                                var bG = SET_BG_COLOR_LIGHT_GREY;
                                var w = SET_BG_COLOR_WHITE;
                                var b = SET_BG_COLOR_BLACK;
                                var fourthPieceW = WHITE_QUEEN;
                                var fourthPieceB = BLACK_QUEEN;
                                var fifthPieceW = WHITE_KING;
                                var fifthPieceB = BLACK_KING;
                                if (playerColor.equals("BLACK")) {
                                    var temp = w;
                                    w = b;
                                    b = temp;
                                    fourthPieceW = WHITE_KING;
                                    fourthPieceB = BLACK_KING;
                                    fifthPieceW = WHITE_QUEEN;
                                    fifthPieceB = BLACK_QUEEN;
                                }
                                var wP = SET_TEXT_COLOR_ORANGE;
                                var bP = SET_TEXT_COLOR_MAGENTA;
                                String blackWhiteToBlackRow = w + bP + BLACK_ROOK +
                                        b + bP + BLACK_KNIGHT +
                                        w + bP + BLACK_BISHOP +
                                        b + bP + fourthPieceB +
                                        w + bP + fifthPieceB +
                                        b + bP + BLACK_BISHOP +
                                        w + bP + BLACK_KNIGHT +
                                        b + bP + BLACK_ROOK;
                                String blackBlackToWhiteRow = b + bP + BLACK_PAWN +
                                        w + bP + BLACK_PAWN +
                                        b + bP + BLACK_PAWN +
                                        w + bP + BLACK_PAWN +
                                        b + bP + BLACK_PAWN +
                                        w + bP + BLACK_PAWN +
                                        b + bP + BLACK_PAWN +
                                        w + bP + BLACK_PAWN;
                                String emptyBlackToWhiteRow = b + EMPTY +
                                        w + EMPTY +
                                        b + EMPTY +
                                        w + EMPTY +
                                        b + EMPTY +
                                        w + EMPTY +
                                        b + EMPTY +
                                        w + EMPTY;
                                String emptyWhiteToBlackRow = w + EMPTY +
                                        b + EMPTY +
                                        w + EMPTY +
                                        b + EMPTY +
                                        w + EMPTY +
                                        b + EMPTY +
                                        w + EMPTY +
                                        b + EMPTY;
                                String whiteWhiteToBlackRow = wP +
                                        w + WHITE_PAWN +
                                        b + WHITE_PAWN +
                                        w + WHITE_PAWN +
                                        b + WHITE_PAWN +
                                        w + WHITE_PAWN +
                                        b + WHITE_PAWN +
                                        w + WHITE_PAWN +
                                        b + WHITE_PAWN;
                                String whiteBlackToWhiteRow = b + wP + WHITE_ROOK +
                                        w + WHITE_KNIGHT +
                                        b + WHITE_BISHOP +
                                        w + fourthPieceW +
                                        b + fifthPieceW +
                                        w + WHITE_BISHOP +
                                        b + WHITE_KNIGHT +
                                        w + WHITE_ROOK;
                                if (!playerColor.equals("BLACK")) {
                                    chessBoardCreator(bG,
                                            blackWhiteToBlackRow,
                                            blackBlackToWhiteRow,
                                            emptyBlackToWhiteRow,
                                            emptyWhiteToBlackRow,
                                            whiteWhiteToBlackRow,
                                            whiteBlackToWhiteRow,
                                            true);
                                } else {
                                    chessBoardCreator(bG,
                                            whiteBlackToWhiteRow,
                                            whiteWhiteToBlackRow,
                                            emptyWhiteToBlackRow,
                                            emptyBlackToWhiteRow,
                                            blackBlackToWhiteRow,
                                            blackWhiteToBlackRow,
                                            false);
                                }
                            } else {
                                System.out.println("Someone has already taken that spot or you misentered your command" +
                                        SET_BG_COLOR_BLACK + "\n");
                            }
                        }
                    }
                }
                case ("help") -> {
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
                case ("clear") -> {
                    System.out.println("You have selected clear, enter your manager password to clear" +
                            SET_BG_COLOR_BLACK);

                    result = scanner.next();
                    if (!Objects.equals(result,
                            "youmustacceptthatbydoingthisyouarejepreodizingthesaveinformationofall2peoplethatwilleventuallyusethis..." +
                                    "thinkcarefu11ybeforeyoueneter")) {
                        System.out.println("password incorrect, you will now be exited from the program" +
                                SET_BG_COLOR_BLACK);
                        finished = true;
                    } else {
                        System.out.println("password accepted, clearing now\n");

                        String registerUrl = serverUrl + "/db";

                        var request = HttpRequest.newBuilder()
                                .uri(new URI(registerUrl))
                                .header("Authorization", userAuth)
                                .timeout(java.time.Duration.ofMillis(5000))
                                .DELETE()
                                .build();

                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    }

                }
                default -> {
                    System.out.println(result + " is not a valid command, use help to see all valid commands\n");
                }
            }
        }
    }

    private void chessBoardCreator(String bG,
                                   String blackWhiteToBlackRow,
                                   String blackBlackToWhiteRow,
                                   String emptyBlackToWhiteRow,
                                   String emptyWhiteToBlackRow,
                                   String whiteWhiteToBlackRow,
                                   String whiteBlackToWhiteRow,
                                   Boolean white) {
        String border = " a   b  c   d   e   f  g   h    " + SET_BG_COLOR_BLACK;
        int startLabel = 8;
        int labelIncrement = -1;
        if (!white) {
            border = " h   g  f   e   d   c  b   a    " + SET_BG_COLOR_BLACK;
            startLabel = 1;
            labelIncrement = 1;
        }
        var pC = SET_TEXT_COLOR_BLACK;
        var bC = SET_BG_COLOR_BLACK;
        System.out.printf(bG + pC + "   " + border + "\n");
        System.out.printf(bG + pC + " %d " + blackWhiteToBlackRow + bG + pC + " %d " + bC + " " + "\n", startLabel, startLabel);
        System.out.printf(bG + pC + " %d " + blackBlackToWhiteRow + bG + pC + " %d " + bC + " " + "\n", startLabel + labelIncrement, startLabel + labelIncrement);
        System.out.printf(bG + pC + " %d " + emptyWhiteToBlackRow + bG + " %d " + bC + " " + "\n", startLabel + (2 * labelIncrement) , startLabel + (2 * labelIncrement));
        System.out.printf(bG + pC + " %d " + emptyBlackToWhiteRow + bG + " %d " + bC + " " + "\n", startLabel + (3 * labelIncrement) , startLabel + (3 * labelIncrement));
        System.out.printf(bG + pC + " %d " + emptyWhiteToBlackRow + bG + " %d " + bC + " " + "\n", startLabel + (4 * labelIncrement) , startLabel + (4 * labelIncrement));
        System.out.printf(bG + pC + " %d " + emptyBlackToWhiteRow + bG + " %d " + bC + " " + "\n", startLabel + (5 * labelIncrement) , startLabel + (5 * labelIncrement));
        System.out.printf(bG + pC + " %d " + whiteWhiteToBlackRow + bG + pC + " %d " + bC + " " + "\n", startLabel + (6 * labelIncrement) , startLabel + (6 * labelIncrement));
        System.out.printf(bG + pC + " %d " + whiteBlackToWhiteRow + bG + pC + " %d " + bC + " " + "\n", startLabel + (7 * labelIncrement) , startLabel + (7 * labelIncrement));
        System.out.printf(bG + pC + "   " + border + "\n");
    }
    /*
    public void errorHandler(HttpResponse<String> response) {
        int errorCode = response.statusCode();
        System.out.println(response.body());
    }

    public void gameMode() {
        return;
    }*/
}
