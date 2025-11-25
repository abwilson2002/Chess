package client;

import chess.*;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.ListResponse;
import static client.ui.EscapeSequences.*;

import static chess.ChessPiece.PieceType.*;

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

    public String user = null;
    boolean signedIn = false;
    private String userAuth = "";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String serverUrl;
    boolean loggedIn = false;
    private String playerColor = null;

    public MainBackground(String serverName) throws Exception {

        serverUrl = serverName;

    }


    public void addUser(String result, String pathedUrl, String requestInput) {
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

    public void gameAction(String result, String pathedUrl, String requestInput, String gameID) {
        var gson = new Gson();
        if (!loggedIn) {
            System.out.println("Please log in or register before continuing" +
                    SET_BG_COLOR_BLACK + "\n");
            return;
        }
        try {
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
                        ChessBoard blankBoard = new ChessBoard();
                        blankBoard.resetBoard();
                        boardPrinter(blankBoard);
                        gameMode(gameID);
                    } else {
                        System.out.println("Someone has already taken that spot or you misentered your command" +
                                SET_BG_COLOR_BLACK + "\n");
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

    public boolean clear() {
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
        return true;
    }


    public void running() throws URISyntaxException, IOException, InterruptedException {

    }

    /*private void chessBoardCreator(String bG,
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
    public void errorHandler(HttpResponse<String> response) {
        int errorCode = response.statusCode();
        System.out.println(response.body());
    }
    */

    private void boardPrinter(ChessBoard board) {
        var bG = SET_BG_COLOR_LIGHT_GREY;
        var wBG = SET_BG_COLOR_WHITE;
        var bBG = SET_BG_COLOR_BLACK;
        int startLetter = 1;
        int endLetter = 9;
        int startNumber = 8;
        int endNumber = 0;
        int directionLetter = 1;
        int directionNumber = -1;
        boolean white = true;
        String letters = "   a   b  c   d   e   f  g   h    " + SET_BG_COLOR_BLACK;
        if (Objects.equals(playerColor, "BLACK")) {
            startLetter = 8;
            endLetter = 0;
            startNumber = 1;
            endNumber = 9;
            directionLetter = -1;
            directionNumber = 1;
            white = false;
            letters = "  h   g  f   e   d   c  b   a    " + SET_BG_COLOR_BLACK;
        }
        System.out.println(SET_TEXT_COLOR_BLACK + letters);
        for (int i = startNumber; (white ? (i > endNumber) : (i < endNumber)); i += directionNumber) {
            System.out.printf(bG + " " + SET_TEXT_COLOR_BLACK + i + " ");
            for (int j = startLetter; (white ? (j < endLetter) : (j > endLetter)); j += directionLetter) {
                var place = new ChessPosition(i, j);
                if (tileColor(i, j)) {
                    System.out.printf(wBG + pieceName(place, board));
                } else {
                    System.out.printf(bBG + pieceName(place, board));
                }
            }
            System.out.printf(bG + " " + SET_TEXT_COLOR_BLACK + i + " " + "\n");
        }
        System.out.println(SET_TEXT_COLOR_BLACK + letters);
    }

    private boolean tileColor(int i, int j) {
        boolean iCheck = (i % 2 == 0);
        boolean jCheck = (j % 2 == 0);
        return iCheck == jCheck;
    }

    private String pieceName(ChessPosition place, ChessBoard board) {
        String encodedPiece = EMPTY;
        ChessPiece piece = board.getPiece(place);
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

    public void gameMode(String gameID) {




    }
}
