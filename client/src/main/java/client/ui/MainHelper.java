package client.ui;

import chess.*;
import client.MainBackground;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;

import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.Scanner;

import static chess.ChessPiece.PieceType.*;
import static chess.ChessPiece.PieceType.BISHOP;
import static chess.ChessPiece.PieceType.KNIGHT;
import static chess.ChessPiece.PieceType.PAWN;
import static chess.ChessPiece.PieceType.QUEEN;
import static chess.ChessPiece.PieceType.ROOK;
import static client.ui.EscapeSequences.*;

public class MainHelper {


    public void move(String[] commands, MainBackground me, String gameID, String userAuth, WebSocket webSocket) {
        var gson = new Gson();
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
            return;
        }

        ChessPosition start = new ChessPosition(
                ((moveStart.charAt(1) - '0')),
                me.letterToNumber((moveStart.charAt(0))));

        ChessPosition end = new ChessPosition(
                ((moveEnd.charAt(1) - '0')),
                me.letterToNumber((moveEnd.charAt(0))));

        if (!promo.equals("null")) {
            promote = ChessPiece.PieceType.valueOf(promo);
        }
        var move = new ChessMove(start, end, promote);

        Integer targetID = Integer.parseInt(gameID);

        String moveString = commands[1] + " to " + commands[2];

        if (promote != null) {
            moveString += " and promoted to " + commands[3];
        }

        UserGameCommand moveCommand = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, userAuth, targetID, move, moveString);

        var moveInput = gson.toJson(moveCommand);

        webSocket.sendText(moveInput, true);
    }

    public void joinErrors(HttpResponse<String> response) {
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

    public void observer(String cR, String gameID, Scanner scanner, WebSocket webSocket,
                         String userAuth, Gson gson, String highlightPosition, MainBackground me) {
        System.out.println(cR + "Observing game " + gameID + SET_BG_COLOR_BLACK);
        boolean stillWatching = true;
        while (stillWatching) {
            var baseCommand = scanner.nextLine().trim();
            String[] command = baseCommand.split("\\s+");
            switch(command[0]) {
                case("help") -> {
                    var text = SET_TEXT_COLOR_YELLOW + SET_BG_COLOR_DARK_GREEN;
                    var bG = SET_BG_COLOR_BLACK;
                    System.out.println(text + "leave : stop observing the game" + bG);
                    System.out.println(text + "highlight <position> : highlights the possible moves at a position" + bG);
                    System.out.println(text + "update : updates the board to the most current state" + bG);
                }
                case("update") -> {
                    var commandType = UserGameCommand.CommandType.LOAD;
                    var uCommand = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID));
                    webSocket.sendText(gson.toJson(uCommand), true);
                }
                case("leave") -> {
                    var commandType = UserGameCommand.CommandType.LEAVE;
                    var lCommand = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID), "observer");
                    webSocket.sendText(gson.toJson(lCommand), true);
                    stillWatching = false;
                    try {
                        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing").join();
                    } catch (Exception ex) {
                        return;
                    }
                }
                case("highlight") -> {
                    highlightPosition = command[1];
                    Integer firstNumber = me.letterToNumber(highlightPosition.charAt(0));
                    String position = String.valueOf((firstNumber*10) + (highlightPosition.charAt(1) - '0'));
                    var commandType = UserGameCommand.CommandType.HIGHLIGHT;
                    var hCommand = new UserGameCommand(commandType, userAuth, Integer.parseInt(gameID), position);
                    webSocket.sendText(gson.toJson(hCommand), true);
                }
                default -> {
                    System.out.println("Not a valid command");
                }
            }
        }
    }

    public void printer(String[] commands) {
        var text = SET_BG_COLOR_DARK_GREEN + SET_TEXT_COLOR_YELLOW;
        var bG = SET_BG_COLOR_BLACK;
        if (commands.length == 1) {
            System.out.println(text + "update : updates the board to the most current state" + bG);
            System.out.println(text + "resign : forfeits the game for you" + bG);
            System.out.println(text + "leave : the cowards way out, leave without forfeiting" + bG);
            System.out.println(text + "highlight <position> : highlights the possible moves at a position" + bG);
            System.out.println(text + "move <move from position> <move to position> <promotion piece> : " +
                    "move a piece, see help(2) for details" + bG);
            System.out.println(text + "help page2 : shows more details about how to input commands" + bG + "\n");
        } else {
            System.out.println(text + "<position> : type in the position using the number then the letter (ie. 2f or 6a" + bG);
            System.out.println(text + "promotion : If you can promote, type in the piece's promotion in all caps (ie. QUEEN)" + bG);
            System.out.println(text + "            note: leave blank if you cannot promote" + bG + "\n");
        }
    }

    public String pieceName(ChessPosition place, Map<String, ChessPiece> board) {
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

    public void helper(boolean finished, String logcase, String serverUrl, Gson gson, MainBackground bg) {
        while(!finished) {
            System.out.printf(SET_BG_COLOR_DARK_GREEN + SET_TEXT_COLOR_YELLOW + "[" + logcase + "] " + "What is your command?" +
                    SET_BG_COLOR_BLACK + "\n");
            var scanner = new Scanner(System.in);
            var result = scanner.next();
            switch (result) {
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
                    bg.addUser(pathedUrl, requestInput);
                    logcase = "Logged in";
                }
                case ("logout") -> {
                    bg.logoutUser();
                    logcase = "Logged out";
                }
                case ("list"), ("create"), ("join") -> {
                    String requestInput = "";
                    String gameID = "";
                    String playerColor = "";
                    if (result.equals("create")) {
                        String gameName = scanner.next();

                        var input = Map.of("username", bg.user, "gameName", gameName);
                        requestInput = gson.toJson(input);
                    } else if (result.equals("join")) {
                        gameID = scanner.next();
                        playerColor = scanner.next();

                        var input = Map.of("username", bg.user, "gameID", gameID, "playerColor", playerColor);
                        requestInput = gson.toJson(input);
                    }
                    bg.gameAction(result, requestInput, gameID, playerColor, scanner);
                }
                case ("help") -> {
                    bg.help();
                }
                case ("clear") -> {
                    bg.clear(scanner);
                }
                default -> {
                    System.out.println(result + " is not a valid command, use help to see all valid commands\n");
                }
            }
        }
    }
}
