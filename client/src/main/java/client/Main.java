package client;
import chess.*;
import com.google.gson.Gson;
import static client.ui.EscapeSequences.*;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.net.http.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        try {

            var bg = new MainBackground(serverUrl);

            String logcase = "Logged Out";
            System.out.println("♕ 240 Chess Client\n");
            Gson gson = new Gson();
            boolean finished = false;
            while (!finished) {
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
                        bg.gameAction(result, requestInput, gameID, playerColor);
                    }
                    case ("help") -> {
                        bg.help();
                    }
                    case ("clear") -> {
                        bg.clear();
                    }
                    default -> {
                        System.out.println(result + " is not a valid command, use help to see all valid commands\n");
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.printf("Error: Unable to start server: %s%n", e.getMessage());
        }




        return;
    }
}